package org.guanzon.cas.gl.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.RecordStatus;
import org.guanzon.cas.gl.services.GLModels;
import org.guanzon.cas.parameter.model.Model_Branch;
import org.guanzon.cas.parameter.services.ParamModels;
import org.json.simple.JSONObject;

public class Model_Recurring_Issuance extends Model {
    Model_Particular poParticular;
    Model_Branch poBranch;
    Model_Payee poPayee;
    
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

            ID = "sPrtclrID";
            ID2 = "sBranchCd";
            ID3 = "sPayeeIDx";
            ID4 = "sAcctNoxx";
            
            poBranch = new ParamModels(poGRider).Branch();
            poPayee = new GLModels(poGRider).Payee();
            poParticular = new GLModels(poGRider).Particular();

            pnEditMode = EditMode.UNKNOWN;
        } catch (SQLException e) {
            logwrapr.severe(e.getMessage());
            System.exit(1);
        }
    }

    public JSONObject setParticularID(String particularId) {
        return setValue("sPrtclrID", particularId);
    }

    public String getParticularID() {
        return (String) getValue("sPrtclrID");
    }
       
    public JSONObject setBranchCode(String branchCode){
        return setValue("sBranchCd", branchCode);
    }

    public String getBranchCode() {
        return (String) getValue("sBranchCd");
    }
    
    public JSONObject setPayeeID(String payeeId){
        return setValue("sPayeeIDx", payeeId);
    }

    public String getPayeeID() {
        return (String) getValue("sPayeeIDx");
    }
    
    public JSONObject setAccountNo(String accountNo){
        return setValue("sAcctNoxx", accountNo);
    }

    public String getAccountNo() {
        return (String) getValue("sAcctNoxx");
    }
    
    public JSONObject setAccountName(String accountName){
        return setValue("sAcctName", accountName);
    }

    public String getAccountName() {
        return (String) getValue("sAcctName");
    }
    
    public JSONObject setAmount(Number amount){
        return setValue("nAmountxx", amount);
    }

    public Number getAmount() {
        return (Number) getValue("nAmountxx");
    }
    
    public JSONObject setMonth(int month){
        return setValue("nMonthxxx", month);
    }

    public int getMonth() {
        return (int) getValue("nMonthxxx");
    }
    
    public JSONObject setBillingDate(Date billingDate) {
        return setValue("dBillDate", billingDate);
    }

    public Date getBillingDate() {
        return (Date) getValue("dBillDate");
    }
    
    public JSONObject setDueDate(Date dueDate) {
        return setValue("dDueUntil", dueDate);
    }

    public Date getDueDate() {
        return (Date) getValue("dDueUntil");
    }
    
    public JSONObject setVATRate(Number rate){
        return setValue("nVATRatex", rate);
    }

    public Number getVATRate() {
        return (Number) getValue("nVATRatex");
    }
    
    public JSONObject setTaxWithheldRate (Number rate){
        return setValue("nTWHldRte", rate);
    }

    public Number getTaxWithheldRate() {
        return (Number) getValue("nTWHldRte");
    }
    
    public JSONObject setLastPRFTrans(String lastPRFTrans){
        return setValue("sLastRqNo", lastPRFTrans);
    }

    public String getLastPRFTrans() {
        return (String) getValue("sLastRqNo");
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
    
    @Override
    public String getNextCode(){
        return ""; 
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
    
    public Model_Branch Branch() throws SQLException, GuanzonException{
        if (!"".equals((String) getValue("sBranchCd"))){
            if (poBranch.getEditMode() == EditMode.READY && 
                poBranch.getBranchCode().equals((String) getValue("sBranchCd")))
                return poBranch;
            else{
                poJSON = poBranch.openRecord((String) getValue("sBranchCd"));

                if ("success".equals((String) poJSON.get("result")))
                    return poBranch;
                else {
                    poBranch.initialize();
                    return poBranch;
                }
            }
        } else {
            poBranch.initialize();
            return poBranch;
        }
    }
    
    public Model_Payee Payee() throws SQLException, GuanzonException{
        if (!"".equals((String) getValue("sPayeeIDx"))){
            if (poPayee.getEditMode() == EditMode.READY && 
                poPayee.getPayeeID().equals((String) getValue("sPayeeIDx")))
                return poPayee;
            else{
                poJSON = poPayee.openRecord((String) getValue("sPayeeIDx"));

                if ("success".equals((String) poJSON.get("result")))
                    return poPayee;
                else {
                    poPayee.initialize();
                    return poPayee;
                }
            }
        } else {
            poPayee.initialize();
            return poPayee;
        }
    }
    
    public JSONObject openRecordByParticular(String Id1, Object Id2,Object Id3) throws SQLException, GuanzonException  {
        poJSON = new JSONObject();

        String lsSQL = MiscUtil.makeSelect(this);

        //replace the condition based on the primary key column of the record
        lsSQL = MiscUtil.addCondition(lsSQL, "sPrtclrID = " + SQLUtil.toSQL(Id1) +
                                                " AND sBranchCd = " + SQLUtil.toSQL(Id2) + 
                                                 " AND sPayeeIDx = " + SQLUtil.toSQL(Id3));

        ResultSet loRS = poGRider.executeQuery(lsSQL);

        try {
            if (loRS.next()) {
                for (int lnCtr = 1; lnCtr <= loRS.getMetaData().getColumnCount(); lnCtr++) {
                    setValue(lnCtr, loRS.getObject(lnCtr));
                }
                
                MiscUtil.close(loRS);

                pnEditMode = EditMode.READY;

                poJSON = new JSONObject();
                poJSON.put("result", "success");
                poJSON.put("message", "Record loaded successfully.");
            } else {
                poJSON = new JSONObject();
                poJSON.put("result", "error");
                poJSON.put("message", "No record to load.");
            }
        } catch (SQLException e) {
//            logError(getCurrentMethodName() + "»" + e.getMessage());
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", e.getMessage());
        }

        return poJSON;
    }
}