package org.guanzon.cas.gl.model;

import java.sql.SQLException;
import java.util.Date;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.RecordStatus;
import org.guanzon.cas.client.model.Model_Client_Master;
import org.guanzon.cas.client.services.ClientModels;
import org.guanzon.cas.gl.services.GLModels;
import org.json.simple.JSONObject;

public class Model_Payee extends Model {
    Model_Client_Master poClient;
    Model_Client_Master poAPClient;
    Model_Particular poParticular;
    
    @Override
    public void initialize() {
        try {
            poEntity = MiscUtil.xml2ResultSet(System.getProperty("sys.default.path.metadata") + XML, getTable());

            poEntity.last();
            poEntity.moveToInsertRow();

            MiscUtil.initRowSet(poEntity);

            //assign default values
            poEntity.updateObject("cRecdStat", RecordStatus.ACTIVE);
            //end - assign default values

            poEntity.insertRow();
            poEntity.moveToCurrentRow();

            poEntity.absolute(1);

            ID = "sPayeeIDx";
            
            ClientModels model = new ClientModels(poGRider);
            poClient = model.ClientMaster();
            poAPClient = model.ClientMaster();
            
            GLModels gl = new GLModels(poGRider);
            poParticular = gl.Particular();

            pnEditMode = EditMode.UNKNOWN;
        } catch (SQLException e) {
            logwrapr.severe(e.getMessage());
            System.exit(1);
        }
    }

    public JSONObject setPayeeID(String payeeId) {
        return setValue("sPayeeIDx", payeeId);
    }

    public String getPayeeID() {
        return (String) getValue("sPayeeIDx");
    }
       
    public JSONObject setPayeeName(String payeeName){
        return setValue("sPayeeNme", payeeName);
    }

    public String getPayeeName() {
        return (String) getValue("sPayeeNme");
    }
    
    public JSONObject setParticularID(String particularId){
        return setValue("sPrtclrID", particularId);
    }

    public String getParticularID() {
        return (String) getValue("sPrtclrID");
    }
    
    public JSONObject setAPClientID(String apClientId){
        return setValue("sAPClntID", apClientId);
    }

    public String getAPClientID() {
        return (String) getValue("sAPClntID");
    }
    
    public JSONObject setClientID(String clientId){
        return setValue("sClientID", clientId);
    }

    public String getClientID() {
        return (String) getValue("sClientID");
    }
        
    public JSONObject setRecordStatus(String recordStatus){
        return setValue("cRecdStat", recordStatus);
    }

    public String getRecordStatus() {
        return (String) getValue("cRecdStat");
    }

    public JSONObject setModifyingId(String modifyingId) {
        return setValue("sModified", modifyingId);
    }

    public String getModifyingId() {
        return (String) getValue("sModified");
    }

    public JSONObject setModifiedDate(Date modifiedDate) {
        return setValue("dModified", modifiedDate);
    }

    public Date getModifiedDate() {
        return (Date) getValue("dModified");
    }
    
    public Model_Particular Particular() throws SQLException, GuanzonException{
        if (!"".equals((String) getValue("sPrtclrID"))){
            if (poParticular.getEditMode() == EditMode.READY && 
                poParticular.getParticularID().equals((String) getValue("sPrtclrID")))
                return poParticular;
            else{
                poJSON = poParticular.openRecord((String) getValue("sPrtclrID"));

                if ("success".equals((String) poJSON.get("result")))
                    return poParticular;
                else {
                    poParticular.initialize();
                    return poParticular;
                }
            }
        } else {
            poParticular.initialize();
            return poParticular;
        }
    }
    
    public Model_Client_Master APClient() throws SQLException, GuanzonException{
        if (!"".equals((String) getValue("sAPClntID"))){
            if (poAPClient.getEditMode() == EditMode.READY && 
                poAPClient.getClientId().equals((String) getValue("sAPClntID")))
                return poAPClient;
            else{
                poJSON = poAPClient.openRecord((String) getValue("sAPClntID"));

                if ("success".equals((String) poJSON.get("result")))
                    return poAPClient;
                else {
                    poAPClient.initialize();
                    return poAPClient;
                }
            }
        } else {
            poAPClient.initialize();
            return poAPClient;
        }
    }
    
    public Model_Client_Master Client() throws SQLException, GuanzonException{
        if (!"".equals((String) getValue("sClientID"))){
            if (poClient.getEditMode() == EditMode.READY && 
                poClient.getClientId().equals((String) getValue("sClientID")))
                return poClient;
            else{
                poJSON = poClient.openRecord((String) getValue("sClientID"));

                if ("success".equals((String) poJSON.get("result")))
                    return poClient;
                else {
                    poAPClient.initialize();
                    return poClient;
                }
            }
        } else {
            poClient.initialize();
            return poClient;
        }
    }
}