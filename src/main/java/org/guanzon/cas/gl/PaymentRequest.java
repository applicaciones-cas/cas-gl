package org.guanzon.cas.gl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.guanzon.appdriver.agent.services.Transaction;
import org.guanzon.appdriver.agent.systables.SysTableContollers;
import org.guanzon.appdriver.agent.systables.TransactionAttachment;
import org.guanzon.appdriver.agent.systables.TransactionStatusHistory;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.iface.GValidator;
import org.guanzon.cas.gl.model.Model_Payment_Request_Detail;
import org.guanzon.cas.gl.model.Model_Payment_Request_Master;
import org.guanzon.cas.gl.services.GLControllers;
import org.guanzon.cas.gl.services.GLModels;
import org.guanzon.cas.gl.status.PaymentRequestStatus;
import org.guanzon.cas.gl.validator.PaymentRequestValidator;
import org.guanzon.cas.inv.warehouse.StockRequest;
import org.guanzon.cas.inv.warehouse.model.Model_Inv_Stock_Request_Master;
import org.guanzon.cas.inv.warehouse.services.InvWarehouseModels;
import org.guanzon.cas.parameter.Department;
import org.guanzon.cas.parameter.services.ParamControllers;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class PaymentRequest extends Transaction{  
    List<Model_Inv_Stock_Request_Master> poInvStockRequestMaster;
    
    List<TransactionAttachment> paAttachments;
    public JSONObject InitTransaction(){      
        SOURCE_CODE = "PRF";
        
        poMaster = new GLModels(poGRider).PaymentRequestMaster();
        poDetail = new GLModels(poGRider).PaymentRequestDetail();        
        paDetail = new ArrayList<>(); 
        paAttachments = new ArrayList<>();
        return initialize();
    }
    
    public JSONObject NewTransaction() throws CloneNotSupportedException{        
        return newTransaction();
    }
    
    public JSONObject SaveTransaction() throws SQLException, CloneNotSupportedException, GuanzonException{
        return saveTransaction();
    }
    
    public JSONObject OpenTransaction(String transactionNo) throws CloneNotSupportedException, SQLException, GuanzonException {       
        return openTransaction(transactionNo);
    }
    
    
    public JSONObject UpdateTransaction(){
        return updateTransaction();
    }
    
    public JSONObject ConfirmTransaction(String remarks) throws ParseException, SQLException, GuanzonException, CloneNotSupportedException {
        
        poJSON = new JSONObject();
        
        String lsStatus = PaymentRequestStatus.CONFIRMED;
        boolean lbConfirm = true;
        
        if (getEditMode() != EditMode.READY){
            poJSON.put("result", "error");
            poJSON.put("message", "No transacton was loaded.");
            return poJSON;                
        }
        
        if (lsStatus.equals((String) poMaster.getValue("cTranStat"))){    
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already confirmed.");
            return poJSON;                
        }
        
        //validator
        poJSON = isEntryOkay(PaymentRequestStatus.CONFIRMED);
        if (!"success".equals((String) poJSON.get("result"))) return poJSON;
        
        //change status
        poJSON =  statusChange(poMaster.getTable(), (String) poMaster.getValue("sTransNox"), remarks,  lsStatus, !lbConfirm);
        
        if (!"success".equals((String) poJSON.get("result"))) return poJSON;
        
        poJSON = new JSONObject();
        poJSON.put("result", "success");
        
        if (lbConfirm) poJSON.put("message", "Transaction confirmed successfully.");
        else poJSON.put("message", "Transaction confirmation request submitted successfully.");
        
        return poJSON;
    }
    
    public JSONObject PaidTransaction(String remarks) throws ParseException, SQLException, GuanzonException, CloneNotSupportedException {
        
        poJSON = new JSONObject();
        
        String lsStatus = PaymentRequestStatus.PAID;
        boolean lbConfirm = true;
        
        if (getEditMode() != EditMode.READY){
            poJSON.put("result", "error");
            poJSON.put("message", "No transacton was loaded.");
            return poJSON;                
        }
        
        if (lsStatus.equals((String) poMaster.getValue("cTranStat"))){    
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already Paid.");
            return poJSON;                
        }
        
        //validator
        poJSON = isEntryOkay(PaymentRequestStatus.PAID);
        if (!"success".equals((String) poJSON.get("result"))) return poJSON;
        
        //change status
        poJSON =  statusChange(poMaster.getTable(), (String) poMaster.getValue("sTransNox"), remarks,  lsStatus, !lbConfirm);
        
        if (!"success".equals((String) poJSON.get("result"))) return poJSON;
        
        poJSON = new JSONObject();
        poJSON.put("result", "success");
        
        if (lbConfirm) poJSON.put("message", "Transaction Paid successfully.");
        else poJSON.put("message", "Transaction Paid request submitted successfully.");
        
        return poJSON;
    }
    
    public JSONObject CancelTransaction(String remarks) throws ParseException, SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();
        
        String lsStatus = PaymentRequestStatus.CANCELLED;
        boolean lbConfirm = true;
        
        if (getEditMode() != EditMode.READY){
            poJSON.put("result", "error");
            poJSON.put("message", "No transacton was loaded.");
            return poJSON;                
        }
        
        if (lsStatus.equals((String) poMaster.getValue("cTranStat"))){    
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already cancelled.");
            return poJSON;                
        }
        
        poJSON = isEntryOkay(PaymentRequestStatus.CANCELLED);
        if (!"success".equals((String) poJSON.get("result"))) return poJSON;

        poJSON =  statusChange(poMaster.getTable(), (String) poMaster.getValue("sTransNox"), remarks,  lsStatus, !lbConfirm);
        
        if (!"success".equals((String) poJSON.get("result"))) return poJSON;
        
        poJSON = new JSONObject();
        poJSON.put("result", "success");
        
        if (lbConfirm) poJSON.put("message", "Transaction cancelled successfully.");
        else poJSON.put("message", "Transaction cancellation request submitted successfully.");
        
        return poJSON;
    }
    
    public JSONObject VoidTransaction(String remarks) throws ParseException, SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();
        
        String lsStatus = PaymentRequestStatus.VOID;
        boolean lbConfirm = true;
        
        if (getEditMode() != EditMode.READY){
            poJSON.put("result", "error");
            poJSON.put("message", "No transacton was loaded.");
            return poJSON;                
        }
        
        if (lsStatus.equals((String) poMaster.getValue("cTranStat"))){    
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already voided.");
            return poJSON;                
        }
        

        poJSON = isEntryOkay(PaymentRequestStatus.VOID);
        if (!"success".equals((String) poJSON.get("result"))) return poJSON;
        

        poJSON =  statusChange(poMaster.getTable(), (String) poMaster.getValue("sTransNox"), remarks,  lsStatus, !lbConfirm);
        
        if (!"success".equals((String) poJSON.get("result"))) return poJSON;
        
        poJSON = new JSONObject();
        poJSON.put("result", "success");
        
        if (lbConfirm) poJSON.put("message", "Transaction voided successfully.");
        else poJSON.put("message", "Transaction voiding request submitted successfully.");
        
        return poJSON;
    }
      
    public JSONObject PostTransaction(String remarks) throws ParseException, SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();
        
        String lsStatus = PaymentRequestStatus.POSTED;
        boolean lbConfirm = true;
        
        if (getEditMode() != EditMode.READY){
            poJSON.put("result", "error");
            poJSON.put("message", "No transacton was loaded.");
            return poJSON;                
        }
        
        if (lsStatus.equals((String) poMaster.getValue("cTranStat"))){    
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already processed.");
            return poJSON;                
        }
        
        poJSON = isEntryOkay(PaymentRequestStatus.POSTED);
        if (!"success".equals((String) poJSON.get("result"))) return poJSON;
        
        poJSON =  statusChange(poMaster.getTable(), (String) poMaster.getValue("sTransNox"), remarks,  lsStatus, !lbConfirm);
        
        if (!"success".equals((String) poJSON.get("result"))) return poJSON;
        
        poJSON = new JSONObject();
        poJSON.put("result", "success");
        
        if (lbConfirm) poJSON.put("message", "Transaction posted successfully.");
        else poJSON.put("message", "Transaction posting request submitted successfully.");
        
        return poJSON;
    }
    
    
    public JSONObject ReturnTransaction(String remarks) throws ParseException, SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();
        
        String lsStatus = PaymentRequestStatus.RETURNED;
        boolean lbConfirm = true;
        
        if (getEditMode() != EditMode.READY){
            poJSON.put("result", "error");
            poJSON.put("message", "No transacton was loaded.");
            return poJSON;                
        }
        
        if (lsStatus.equals((String) poMaster.getValue("cTranStat"))){    
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already returned.");
            return poJSON;                
        }
        

        poJSON = isEntryOkay(PaymentRequestStatus.RETURNED);
        if (!"success".equals((String) poJSON.get("result"))) return poJSON;
        

        poJSON =  statusChange(poMaster.getTable(), (String) poMaster.getValue("sTransNox"), remarks,  lsStatus, !lbConfirm);
        
        if (!"success".equals((String) poJSON.get("result"))) return poJSON;
        
        poJSON = new JSONObject();
        poJSON.put("result", "success");
        
        if (lbConfirm) poJSON.put("message", "Transaction returned successfully.");
        else poJSON.put("message", "Transaction return request submitted successfully.");
        
        return poJSON;
    }
    
    public JSONObject AddDetail() throws CloneNotSupportedException{
        if (Detail(getDetailCount() - 1).getParticularID().isEmpty()) {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "Last row has empty item.");
            return poJSON;
        }
        return addDetail();
    }
    
    private TransactionAttachment TransactionAttachment()throws SQLException,GuanzonException {
        return new SysTableContollers(poGRider, null).TransactionAttachment();
    }
    
    private List<TransactionAttachment> TransactionAttachmentList() {
        return paAttachments;
    }

    public TransactionAttachment TransactionAttachmentList(int row) {
        return (TransactionAttachment) paAttachments.get(row);
    }

    public int getTransactionAttachmentCount() {
        if (paAttachments == null) {
            paAttachments = new ArrayList<>();
        }

        return paAttachments.size();
    }

    public JSONObject addAttachment()
            throws SQLException,
            GuanzonException {
        poJSON = new JSONObject();

        if (paAttachments.isEmpty()) {
            paAttachments.add(TransactionAttachment());
            poJSON = paAttachments.get(getTransactionAttachmentCount() - 1).newRecord();
        } else {
            if (!paAttachments.get(paAttachments.size() - 1).getModel().getTransactionNo().isEmpty()) {
                paAttachments.add(TransactionAttachment());
            } else {
                poJSON.put("result", "error");
                poJSON.put("message", "Unable to add transaction attachment.");
                return poJSON;
            }
        }

        poJSON.put("result", "success");
        return poJSON;

    }

    public JSONObject loadAttachments()
            throws SQLException,
            GuanzonException {
        poJSON = new JSONObject();
        paAttachments = new ArrayList<>();

        TransactionAttachment loAttachment = new SysTableContollers(poGRider, null).TransactionAttachment();
        List loList = loAttachment.getAttachments(SOURCE_CODE, Master().getTransactionNo());
        for (int lnCtr = 0; lnCtr <= loList.size() - 1; lnCtr++) {
            paAttachments.add(TransactionAttachment());
            poJSON = paAttachments.get(getTransactionAttachmentCount() - 1).openRecord((String) loList.get(lnCtr));
            if ("success".equals((String) poJSON.get("result"))) {
                if(Master().getEditMode() == EditMode.UPDATE){
                   poJSON = paAttachments.get(getTransactionAttachmentCount() - 1).updateRecord();
                }
                System.out.println(paAttachments.get(getTransactionAttachmentCount() - 1).getModel().getTransactionNo());
                System.out.println(paAttachments.get(getTransactionAttachmentCount() - 1).getModel().getSourceNo());
                System.out.println(paAttachments.get(getTransactionAttachmentCount() - 1).getModel().getSourceCode());
                System.out.println(paAttachments.get(getTransactionAttachmentCount() - 1).getModel().getFileName());
            }
        }
        return poJSON;
    }
    
    public JSONObject SearchDepartment(String value, boolean byCode) throws ExceptionInInitializerError, SQLException, GuanzonException{
        Department object = new ParamControllers(poGRider, logwrapr).Department();
        object.setRecordStatus("1");

        poJSON = object.searchRecord(value, byCode);

        if ("success".equals((String) poJSON.get("result"))){
            Master().setDepartmentID(object.getModel().getDepartmentId());
        }    
        
        return poJSON;
    }
    
    public JSONObject SearchPayee(String value, boolean byCode) throws ExceptionInInitializerError, SQLException, GuanzonException{
        Payee object = new GLControllers(poGRider, logwrapr).Payee();
        object.setRecordStatus("1");

        poJSON = object.searchRecord(value, byCode);

        if ("success".equals((String) poJSON.get("result"))){
            Master().setPayeeID(object.getModel().getPayeeID());
        }    
        
        return poJSON;
    }
    
    public JSONObject SearchParticular(String value, boolean byCode,int row) throws ExceptionInInitializerError, SQLException, GuanzonException{
        Particular object = new GLControllers(poGRider, logwrapr).Particular();
        object.setRecordStatus("1");

        poJSON = object.searchRecord(value, byCode);

        if ("success".equals((String) poJSON.get("result"))){
            Detail(row).setParticularID(object.getModel().getParticularID());
        }    
        
        return poJSON;
    }
        
    @Override
    public String getSourceCode(){
        return SOURCE_CODE;
    }    
    
    @Override
    public Model_Payment_Request_Master Master() {
        return (Model_Payment_Request_Master) poMaster;
    }

    @Override
    public Model_Payment_Request_Detail Detail(int row) {
        return (Model_Payment_Request_Detail) paDetail.get(row);
    }    
        
    @Override
    public JSONObject willSave() {
        /*Put system validations and other assignments here*/
        poJSON = new JSONObject();
        
        //remove items with no stockid or quantity order
//        Iterator<Model> detail = Detail().iterator();
//        while (detail.hasNext()) {
//            if ("".equals((String) detail.next().getValue("sStockIDx")) ||
//                (int)detail.next().getValue("nQuantity") <= 0) {
//                detail.remove();
//            }
//        }
//        Iterator<Model> detail = Detail().iterator();
//                while (detail.hasNext()) {
//                    Model item = detail.next(); // Store the item before checking conditions
//
//                    if ("".equals((String) item.getValue("sTransNox"))
//                            || (int) item.getValue("nQuantity") <= 0) {
//                        detail.remove(); // Correctly remove the item
//                    }
//                }


        //assign other info on detail
        for (int lnCtr = 0; lnCtr <= getDetailCount() - 1; lnCtr ++){            
            Detail(lnCtr).setTransactionNo(Master().getTransactionNo());
            Detail(lnCtr).setEntryNo(lnCtr + 1);
        }
        
//        if (getDetailCount() == 1){
//            //do not allow a single item detail with no quantity order
//            if (Detail(0).getQuantityOnHand() == 0) {
//                poJSON.put("result", "error");
//                poJSON.put("message", "Your order has zero quantity.");
//                return poJSON;
//            }
//        }

        //attachement checker
        if (getTransactionAttachmentCount() > 0) {
            Iterator<TransactionAttachment> attachment = TransactionAttachmentList().iterator();
            while (attachment.hasNext()) {
                TransactionAttachment item = attachment.next();

                if ((String) item.getModel().getFileName() == null || "".equals(item.getModel().getFileName())) {
                    attachment.remove();
                }
            }
        }
        
        poJSON.put("result", "success");
        return poJSON;
    }
    
    @Override
    public JSONObject save() {
        return isEntryOkay(PaymentRequestStatus.OPEN);
    }
    
    @Override
    public JSONObject saveOthers() {
        poJSON = new JSONObject();
        
        poJSON.put("result", "success");
        return poJSON;
    }
    
    @Override
    public void saveComplete() {
        System.out.println("Transaction saved successfully.");
    }
    
    @Override
    public JSONObject initFields() {
        poJSON = new JSONObject();
        
        poJSON.put("result", "success");
        return poJSON;
    }
    
    @Override
    public void initSQL(){
        SQL_BROWSE = "";
    }
    
    @Override
    protected JSONObject isEntryOkay(String status){
        GValidator loValidator = new PaymentRequestValidator();        
        loValidator.setApplicationDriver(poGRider);
        loValidator.setTransactionStatus(status);
        poJSON = loValidator.validate();
        return poJSON;
    }

  
    public JSONObject getApprovedStockRequests() throws SQLException, GuanzonException {
        StringBuilder lsSQL = new StringBuilder(
                "SELECT" +
               "  sTransNox" +
               ", sBranchCd" +
               ", sIndstCdx" +
               ", sCategrCd" +
               ", dTransact" +
               ", sReferNox" +
               ", sRemarksx" +
               ", sIssNotes" +
               ", nCurrInvx" +
               ", nEstInvxx" +
               ", sApproved" +
               ", dApproved" +
               ", sAprvCode" +
               ", nEntryNox" +
               ", sSourceCd" +
               ", sSourceNo" +
               ", cConfirmd" +
               ", cTranStat" +
               ", sModified" +
               ", dModified" +
               ", dTimeStmp" +
               " FROM inv_stock_request_master");

        lsSQL.append(" ORDER BY sTransNox ASC");

        System.out.println("Executing SQL: " + lsSQL.toString());

        ResultSet loRS = poGRider.executeQuery(lsSQL.toString());
        JSONObject poJSON = new JSONObject();
        JSONArray dataArray = new JSONArray();
        try {
            int lnctr = 0;

            if (MiscUtil.RecordCount(loRS) >= 0) {
                poInvStockRequestMaster = new ArrayList<>();
                while (loRS.next()) {
                    // Print the result set
                    JSONObject request = new JSONObject();
                    System.out.println("sTransNox: " + loRS.getString("sTransNox"));
                    System.out.println("sBranchCd: " + loRS.getString("sBranchCd"));
                    System.out.println("dTransact: " + loRS.getDate("dTransact"));
                    System.out.println("nEntryNox: " + loRS.getInt("nEntryNox"));
                    System.out.println("sReferNox: " + loRS.getString("sReferNox"));
                    System.out.println("sApproved: " + loRS.getString("sApproved"));
                    System.out.println("------------------------------------------------------------------------------");

                    poInvStockRequestMaster.add(invStockRequestMaster(loRS.getString("sTransNox")));
                    poInvStockRequestMaster.get(poInvStockRequestMaster.size() - 1)
                            .openRecord(loRS.getString("sTransNox"));
                    dataArray.add(request);
                    lnctr++;
                }

                System.out.println("Records found: " + lnctr);
                poJSON.put("result", "success");
                poJSON.put("message", "Record loaded successfully.");
                poJSON.put("data", dataArray);

            } else {
                poInvStockRequestMaster = new ArrayList<>();
                addInventoryStockRequestMaster();
                poJSON.put("result", "error");
                poJSON.put("continue", true);
                poJSON.put("message", "No record found .");
            }
            MiscUtil.close(loRS);
        } catch (SQLException e) {
            poJSON.put("result", "error");
            poJSON.put("message", e.getMessage());
        }
        return poJSON;
    }
    
    private Model_Inv_Stock_Request_Master invStockRequestMaster(String transactionNo) throws SQLException, GuanzonException {
        Model_Inv_Stock_Request_Master object = new InvWarehouseModels(poGRider).InventoryStockRequestMaster();

        JSONObject loJSON = object.openRecord(transactionNo);

        if ("success".equals((String) loJSON.get("result"))) {
            return object;
        } else {
            return new InvWarehouseModels(poGRider).InventoryStockRequestMaster();
        }
    }
    
    private Model_Inv_Stock_Request_Master invStockRequestMaster() {
        return new InvWarehouseModels(poGRider).InventoryStockRequestMaster();
    }
    
    public JSONObject addInventoryStockRequestMaster() {
        poJSON = new JSONObject();

        if (poInvStockRequestMaster.isEmpty()) {
            poInvStockRequestMaster.add(invStockRequestMaster());
        } else {
            if (!poInvStockRequestMaster.get(poInvStockRequestMaster.size() - 1).getTransactionNo().isEmpty()) {
                poInvStockRequestMaster.add(invStockRequestMaster());
            } else {
                poJSON.put("result", "error");
                poJSON.put("message", "Unable to add Inventory Stock Request.");
                return poJSON;
            }
        }

        poJSON.put("result", "success");
        return poJSON;
    }
    
    public JSONObject getApprovedStockRequests(String transactionNo) {
       return poJSON; 
    }
public JSONObject addOrdersToDetail(String transactionNo) throws CloneNotSupportedException, SQLException, GuanzonException {
    StockRequest loTrans = new StockRequest();
    poJSON = loTrans.OpenTransaction(transactionNo);

    if (!"success".equals((String) poJSON.get("result"))) {
        return poJSON; 
    }

    poDetail = new GLModels(poGRider).PaymentRequestDetail(); // Initialize poDetail

    for (int lnCtr = 0; lnCtr < loTrans.getDetailCount(); lnCtr++) {
        Model_Payment_Request_Detail detail = new Model_Payment_Request_Detail();
       poDetail.setValue("sSourceNo", transactionNo);
        AddDetail(); 
    }

    poJSON = new JSONObject();
    poJSON.put("result", "success");
    poJSON.put("message", "Orders added successfully to Purchase Order details.");
    return poJSON;
}
}