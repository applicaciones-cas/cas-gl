package org.guanzon.cas.gl;

import java.sql.SQLException;
import org.guanzon.appdriver.agent.ShowDialogFX;
import org.guanzon.appdriver.agent.services.Parameter;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.Logical;
import org.guanzon.appdriver.constant.UserRight;
import org.guanzon.cas.gl.model.Model_Account_Chart;
import org.guanzon.cas.gl.services.GLModels;
import org.json.simple.JSONObject;

public class AccountChart extends Parameter{
    Model_Account_Chart poModel;
    
    @Override
    public void initialize() {
        psRecdStat = Logical.YES;
        
        GLModels model = new GLModels(poGRider);
        poModel = model.Account_Chart();
    }
    
    @Override
    public JSONObject isEntryOkay() throws SQLException {
        poJSON = new JSONObject();
        
        if (poGRider.getUserLevel() < UserRight.SYSADMIN){
            poJSON.put("result", "error");
            poJSON.put("message", "User is not allowed to save record.");
            return poJSON;
        } else {
            poJSON = new JSONObject();
            
            if (poModel.getAccountCode().isEmpty()){
                poJSON.put("result", "error");
                poJSON.put("message", "Account code must not be empty.");
                return poJSON;
            }
            
            if (poModel.getDescription() == null ||  poModel.getDescription().isEmpty()){
                poJSON.put("result", "error");
                poJSON.put("message", "Description must not be empty.");
                return poJSON;
            }
        }
        
        poModel.setModifyingId(poGRider.Encrypt(poGRider.getUserID()));
        poModel.setModifiedDate(poGRider.getServerDate());
        
        poJSON.put("result", "success");
        return poJSON;
    }
    
    @Override
    public Model_Account_Chart getModel() {
        return poModel;
    }
    
    @Override
    public JSONObject searchRecord(String value, boolean byCode) throws SQLException, GuanzonException{
        String lsSQL = getSQ_Browse();
        
        poJSON = ShowDialogFX.Search(poGRider,
                lsSQL,
                value,
                "ID»Description»GL Code»Industry",
                "sAcctCode»sDescript»sGLCodexx»xIndstNme",
                "a.sAcctCode»a.sDescript»a.sGLCodexx»IFNULL(b.sDescript, '')",
                byCode ? 0 : 1);

        if (poJSON != null) {
            return poModel.openRecord((String) poJSON.get("sAcctCode"));
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;
        }
    }
    
    @Override
    public String getSQ_Browse(){
        String lsCondition = "";

        if (psRecdStat.length() > 1) {
            for (int lnCtr = 0; lnCtr <= psRecdStat.length() - 1; lnCtr++) {
                lsCondition += ", " + SQLUtil.toSQL(Character.toString(psRecdStat.charAt(lnCtr)));
            }

            lsCondition = "a.cRecdStat IN (" + lsCondition.substring(2) + ")";
        } else {
            lsCondition = "a.cRecdStat = " + SQLUtil.toSQL(psRecdStat);
        }
        
        String lsSQL = "SELECT" +
                            "  a.sAcctCode" +
                            ", a.sDescript" +
                            ", a.sParentCd" +
                            ", a.cBasedAct" +
                            ", a.sAcctGrpx" +
                            ", a.sReprtGrp" +
                            ", a.cAcctType" +
                            ", a.cBalTypex" +
                            ", a.sGLCodexx" +
                            ", a.sIndstCde" +
                            ", a.cRecdStat" +
                            ", a.sModified" +
                            ", a.dModified" +
                            ", IFNULL(b.sDescript, '') xIndstNme" +
                        " FROM Account_Chart a" +
                            " LEFT JOIN Industry b ON a.sIndstCde = b.sIndstCdx";
        
        return MiscUtil.addCondition(lsSQL, lsCondition);
    }
}