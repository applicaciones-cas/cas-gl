package org.guanzon.cas.gl;

import java.sql.SQLException;
import org.guanzon.appdriver.agent.ShowDialogFX;
import org.guanzon.appdriver.agent.services.Parameter;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.constant.Logical;
import org.guanzon.appdriver.constant.UserRight;
import org.guanzon.cas.gl.model.Model_Transaction_Account_Chart;
import org.guanzon.cas.gl.services.GLModels;
import org.json.simple.JSONObject;

public class TransactionAccountChart extends Parameter{
    Model_Transaction_Account_Chart poModel;
    
    @Override
    public void initialize() {
        psRecdStat = Logical.YES;
        
        GLModels model = new GLModels(poGRider);
        poModel = model.Transaction_Account_Chart();
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
            
            if (poModel.getGLCode()== null ||  poModel.getGLCode().isEmpty()){
                poJSON.put("result", "error");
                poJSON.put("message", "Code must not be empty.");
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
    public Model_Transaction_Account_Chart getModel() {
        return poModel;
    }
    
    
    
    @Override
    public JSONObject searchRecord(String value, boolean byCode) throws SQLException, GuanzonException{
        String lsSQL = getSQ_Browse();
        
        poJSON = ShowDialogFX.Search(poGRider,
                lsSQL,
                value,
                "ID»Description",
                "sGLCodexx»sGLDescxx",
                "sGLCodexx»sGLDescxx",
                byCode ? 0 : 1);

        if (poJSON != null) {
            return poModel.openRecord((String) poJSON.get("sGLCodexx"));
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;
        }
    }
}