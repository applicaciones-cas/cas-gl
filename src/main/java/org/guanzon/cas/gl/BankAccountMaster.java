package org.guanzon.cas.gl;

import java.sql.SQLException;
import org.guanzon.appdriver.agent.ShowDialogFX;
import org.guanzon.appdriver.agent.services.Parameter;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.Logical;
import org.guanzon.appdriver.constant.UserRight;
import org.guanzon.cas.gl.model.Model_Bank_Account_Master;
import org.guanzon.cas.gl.services.GLModels;
import org.json.simple.JSONObject;

public class BankAccountMaster extends Parameter{
    Model_Bank_Account_Master poModel;
    
    @Override
    public void initialize() throws SQLException, GuanzonException {
        psRecdStat = Logical.YES;
        
        GLModels model = new GLModels(poGRider);
        poModel = model.Bank_Account_Master();
        
        super.initialize();
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
            
            if (poModel.getBankAccountId()== null ||  poModel.getBankAccountId().isEmpty()){
                poJSON.put("result", "error");
                poJSON.put("message", "Account must not be empty.");
                return poJSON;
            }
            
            if (poModel.getIndustryCode() == null ||  poModel.getIndustryCode().isEmpty()){
                poJSON.put("result", "error");
                poJSON.put("message", "Industry must not be empty.");
                return poJSON;
            }
            
            if (poModel.getBranchCode() == null ||  poModel.getBranchCode().isEmpty()){
                poJSON.put("result", "error");
                poJSON.put("message", "Branch must not be empty.");
                return poJSON;
            }
            
            if (poModel.getCompanyId() == null ||  poModel.getCompanyId().isEmpty()){
                poJSON.put("result", "error");
                poJSON.put("message", "Company must not be empty.");
                return poJSON;
            }
            
            if (poModel.getBankId() == null ||  poModel.getBankId().isEmpty()){
                poJSON.put("result", "error");
                poJSON.put("message", "Bank must not be empty.");
                return poJSON;
            }
            
            if (poModel.getAccountNo() == null ||  poModel.getAccountNo().isEmpty()){
                poJSON.put("result", "error");
                poJSON.put("message", "Account number must not be empty.");
                return poJSON;
            }
            
            if (poModel.getAccountName() == null ||  poModel.getAccountName().isEmpty()){
                poJSON.put("result", "error");
                poJSON.put("message", "Account name must not be empty.");
                return poJSON;
            }
        }
        
        poModel.setModifyingId(poGRider.Encrypt(poGRider.getUserID()));
        poModel.setModifiedDate(poGRider.getServerDate());
        
        poJSON.put("result", "success");
        return poJSON;
    }
    
    @Override
    public Model_Bank_Account_Master getModel() {
        return poModel;
    }
    
    @Override
    public JSONObject searchRecord(String value, boolean byCode) throws SQLException, GuanzonException{
        String lsSQL = getSQ_Browse();
        
        poJSON = ShowDialogFX.Search(poGRider,
                lsSQL,
                value,
                "ID»Bank»Account No.»Account Name",
                "sBnkActID»xBankName»sActNumbr»sActNamex",
                "a.sBnkActID»IFNULL(b.sBankName, '')»a.sActNumbr»a.sActNamex",
                byCode ? 0 : 1);

        if (poJSON != null) {
            return poModel.openRecord((String) poJSON.get("sBnkActID"));
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
                            "  a.sBnkActID" +
                            ", a.sBankIDxx" +
                            ", a.sActNumbr" +
                            ", a.sActNamex" +
                            ", IFNULL(b.sBankName, '') xBankName" +
                        " FROM Bank_Account_Master a" +
                            " LEFT JOIN Banks b ON a.sBankIDxx = b.sBankIDxx";
        
        
        return MiscUtil.addCondition(lsSQL, lsCondition);
    }
}