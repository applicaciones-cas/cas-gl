/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.guanzon.cas.gl.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.cas.client.model.Model_Client_Master;
import org.guanzon.cas.client.services.ClientModels;
import org.guanzon.cas.gl.services.GLModels;
import org.guanzon.cas.gl.status.DisbursementStatic;
import org.guanzon.cas.parameter.model.Model_Banks;
import org.guanzon.cas.parameter.model.Model_Branch;
import org.guanzon.cas.parameter.services.ParamModels;
import org.json.simple.JSONObject;

/**
 *
 * @author User
 */
public class Model_Check_Payments extends Model{
    Model_Client_Master poSupplier;    
    Model_Payee poPayee;    
    Model_Branch poBranch;
    Model_Banks poBanks;
    
    @Override
    public void initialize() {
        try {
            poEntity = MiscUtil.xml2ResultSet(System.getProperty("sys.default.path.metadata") + XML, getTable());
            
            poEntity.last();
            poEntity.moveToInsertRow();

            MiscUtil.initRowSet(poEntity);
            
            poEntity.updateObject("dTransact", SQLUtil.toDate(xsDateShort(poGRider.getServerDate()), SQLUtil.FORMAT_SHORT_DATE));
            poEntity.updateObject("dCheckDte", SQLUtil.toDate(xsDateShort(poGRider.getServerDate()), SQLUtil.FORMAT_SHORT_DATE));
            
            poEntity.updateObject("nAmountxx", DisbursementStatic.DefaultValues.default_value_double_0000);
            poEntity.updateString("cTranStat", DisbursementStatic.OPEN);
            poEntity.updateObject("sBranchCd", poGRider.getBranchCode());
            
            poEntity.insertRow();
            poEntity.moveToCurrentRow();

            poEntity.absolute(1);
            ID = "sTransNox";

            ParamModels model = new ParamModels(poGRider);
            poBranch = model.Branch();
            poBanks = model.Banks();
            GLModels gl = new GLModels(poGRider);
            poPayee = gl.Payee();   
            ClientModels clientModel = new ClientModels(poGRider);
            poSupplier = clientModel.ClientMaster();
            
            pnEditMode = EditMode.UNKNOWN;
        } catch (SQLException e) {
            logwrapr.severe(e.getMessage());
            System.exit(1);
        }
    }
    
    private static String xsDateShort(Date fdValue) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String date = sdf.format(fdValue);
        return date;
    }
    
    public JSONObject setTransactionNo(String transactionNo){
        return setValue("sTransNox", transactionNo);
    }
    
    public String getTransactionNo  (){
        return (String) getValue("sTransNox");
    }
        
    public JSONObject setBranchCode(String branchCode){
        return setValue("sBranchCd", branchCode);
    }
    
    public String getBranchCode(){
        return (String) getValue("sBranchCd");
    }
    
    public JSONObject setTransactionDate(Date transactionDate){
        return setValue("dTransact", transactionDate);
    }
    
    public Date getTransactionDate(){
        return (Date) getValue("dTransact");
    }

    
    public JSONObject setBankID(String bankID){
        return setValue("sBankIDxx", bankID);
    }
    
    public String getBankID(){
        return (String) getValue("sBankIDxx");
    }
    
    public JSONObject setBankAcountID(String bankAccountID){
        return setValue("sBnkActID", bankAccountID);
    }
    
    public String getBankAcountID(){
        return (String) getValue("sBnkActID");
    }
    
    public JSONObject setCheckNo(String checkNo){
        return setValue("sCheckNox", checkNo);
    }
    
    public String getCheckNo(){
        return (String) getValue("sBasCheckNoxnkRefr");
    }
    
    public JSONObject setCheckDate(Date checkDate){
        return setValue("dCheckDte", checkDate);
    }
    
    public Date getCheckDate(){
        return (Date) getValue("dCheckDte");
    }
    
    public JSONObject setPayorID(String payorID){
        return setValue("sPayorIDx", payorID);
    }
    
    public String getPayorID(){
        return (String) getValue("sPayorIDx");
    }
    
    public JSONObject setPayeeID(String payeeID){
        return setValue("sPayeeIDx", payeeID);
    }
    
    public String getPayeeID(){
        return (String) getValue("sPayeeIDx");
    }
    
    public JSONObject setAmount(Number amount){
        return setValue("nAmountxx", amount);
    }   
    
    public Number getAmount(){
        return (Number) getValue("nAmountxx");
    }
    
    public JSONObject setRemarks(String remarks){
        return setValue("sRemarksx", remarks);
    }
    
    public String getRemarks(){
        return (String) getValue("sRemarksx");
    }
    
    public JSONObject setSourceCode(String sourceCode){
        return setValue("sSourceCd", sourceCode);
    }
    
    public String getSourceCode(){
        return (String) getValue("sSourceCd");
    }
    
    public JSONObject setSourceNo(String sourceNo){
        return setValue("sSourceNo", sourceNo);
    }
   
    public String getSourceNo(){
        return (String) getValue("sSourceNo");
    }
    
    public JSONObject setLocation(String location){
        return setValue("cLocation", location);
    }
    
    public String getLocation(){
        return (String) getValue("cLocation");
    }
    
    
    public JSONObject isReplaced(boolean replaced) {
        return setValue("cIsReplcd", replaced);
    }

    public boolean isReplaced() {
        Object value = getValue("cIsReplcd");
        return value instanceof Boolean ? (Boolean) value : false;
    }
    
    public JSONObject setReleased(String released){
        return setValue("cReleased", released);
    }
    
    public String getReleased(){
        return (String) getValue("cReleased");
    }
    
    public JSONObject setPayeeType(String payeeType){
        return setValue("cPayeeTyp", payeeType);
    }
    
    public String getPayeeType(){
        return (String) getValue("cPayeeTyp");
    }
    
        
    public JSONObject setDesbursementMode(String desbursementMode){
        return setValue("cDisbMode", desbursementMode);
    }
    
    public String getDesbursementMode(){
        return (String) getValue("cDisbMode");
    }
        
    public JSONObject setClaimant(String claimant){
        return setValue("cClaimant", claimant);
    }
    
    public String getClaimant(){
        return (String) getValue("cClaimant");
    }
        
    public JSONObject setAuthorize(String authorize){
        return setValue("sAuthorze", authorize);
    }
    
    public String getAuthorize(){
        return (String) getValue("sAuthorze");
    }
    
    public JSONObject isCross(boolean iscross) {
        return setValue("cIsCrossx", iscross);
    }

    public boolean isCross() {
        Object value = getValue("cIsCrossx");
        return value instanceof Boolean ? (Boolean) value : false;
    }
    
    public JSONObject isPayee(boolean ispayee) {
        return setValue("cIsPayeex", ispayee);
    }

    public boolean isPayee() {
        Object value = getValue("cIsPayeex");
        return value instanceof Boolean ? (Boolean) value : false;
    }
    
//    cIsPayeex to verify
    
    public JSONObject setTransactionStatus(String transactionStatus){
        return setValue("cTranStat", transactionStatus);
    }
    
    public String getTransactionStatus(){
        return (String) getValue("cTranStat");
    }
    
    public JSONObject setModifyingId(String modifyingId){
        return setValue("sModified", modifyingId);
    }
    
    public String getModifyingId(){
        return (String) getValue("sModified");
    }
    
    public JSONObject setModifiedDate(Date modifiedDate){
        return setValue("dModified", modifiedDate);
    }
    
    public Date getModifiedDate(){
        return (Date) getValue("dModified");
    }
    
    @Override
    public String getNextCode() {
        return MiscUtil.getNextCode(this.getTable(), ID, true, poGRider.getGConnection().getConnection(), poGRider.getBranchCode());
    }


    public Model_Payee Payee() throws GuanzonException, SQLException {
        if (!"".equals((String) getValue("sPayeeIDx"))) {
            if (poPayee.getEditMode() == EditMode.READY
                    && poPayee.getPayeeID().equals((String) getValue("sPayeeIDx"))) {
                return poPayee;
            } else {
                poJSON = poPayee.openRecord((String) getValue("sPayeeIDx"));
                if ("success".equals((String) poJSON.get("result"))) {
                    return poPayee;
                } else {
                    poPayee.initialize();
                    return poPayee;
                }
            }
        } else {
            poPayee.initialize();
            return poPayee;
        }
    }
    public Model_Client_Master Supplier() throws GuanzonException, SQLException {
        if (!"".equals((String) getValue("sSupplier"))) {
            if (poSupplier.getEditMode() == EditMode.READY
                    && poSupplier.getClientId().equals((String) getValue("sSupplier"))) {
                return poSupplier;
            } else {
                poJSON = poSupplier.openRecord((String) getValue("sSupplier"));
                if ("success".equals((String) poJSON.get("result"))) {
                    return poSupplier;
                } else {
                    poSupplier.initialize();
                    return poSupplier;
                }
            }
        } else {
            poSupplier.initialize();
            return poSupplier;
        }
    }
    
    public Model_Branch Branch() throws GuanzonException, SQLException {
        if (!"".equals((String) getValue("sBranchCd"))) {
            if (poBranch.getEditMode() == EditMode.READY
                    && poBranch.getBranchCode().equals((String) getValue("sBranchCd"))) {
                return poBranch;
            } else {
                poJSON = poBranch.openRecord((String) getValue("sBranchCd"));
                if ("success".equals((String) poJSON.get("result"))) {
                    return poBranch;
                } else {
                    poBranch.initialize();
                    return poBranch;
                }
            }
        } else {
            poBranch.initialize();
            return poBranch;
        }
    }
    
    public Model_Banks Banks() throws GuanzonException, SQLException {
        if (!"".equals((String) getValue("sBankIDxx"))) {
            if (poBanks.getEditMode() == EditMode.READY
                    && poBanks.getBankID().equals((String) getValue("sBankIDxx"))) {
                return poBanks;
            } else {
                poJSON = poBanks.openRecord((String) getValue("sBankIDxx"));
                if ("success".equals((String) poJSON.get("result"))) {
                    return poBanks;
                } else {
                    poBanks.initialize();
                    return poBanks;
                }
            }
        } else {
            poBanks.initialize();
            return poBanks;
        }
    }
    
}
