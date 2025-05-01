package org.guanzon.cas.gl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.guanzon.appdriver.agent.ShowDialogFX;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.agent.services.Transaction;
import org.guanzon.appdriver.agent.systables.SysTableContollers;
import org.guanzon.appdriver.agent.systables.TransactionAttachment;
import org.guanzon.appdriver.agent.systables.TransactionStatusHistory;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.Logical;
import org.guanzon.appdriver.iface.GValidator;
import org.guanzon.cas.gl.model.Model_Payment_Request_Detail;
import org.guanzon.cas.gl.model.Model_Payment_Request_Master;
import org.guanzon.cas.gl.model.Model_Recurring_Issuance;
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
import org.json.simple.parser.ParseException;

public class PaymentRequest extends Transaction {

    List<Model_Inv_Stock_Request_Master> poInvStockRequestMaster;

    List<TransactionAttachment> paAttachments;
    List<Model_Recurring_Issuance> paRecurring;

    public JSONObject InitTransaction() {
        SOURCE_CODE = "PRF";

        poMaster = new GLModels(poGRider).PaymentRequestMaster();
        poDetail = new GLModels(poGRider).PaymentRequestDetail();
        paDetail = new ArrayList<>();
        paAttachments = new ArrayList<>();
        return initialize();
    }

    public JSONObject NewTransaction() throws CloneNotSupportedException {
        return newTransaction();
    }

    public JSONObject SaveTransaction() throws SQLException, CloneNotSupportedException, GuanzonException {
        return saveTransaction();
    }

    public JSONObject OpenTransaction(String transactionNo) throws CloneNotSupportedException, SQLException, GuanzonException {
        return openTransaction(transactionNo);
    }

    public JSONObject UpdateTransaction() {
        return updateTransaction();
    }

    public JSONObject ConfirmTransaction(String remarks) throws ParseException, SQLException, GuanzonException, CloneNotSupportedException {

        poJSON = new JSONObject();

        String lsStatus = PaymentRequestStatus.CONFIRMED;
        boolean lbConfirm = true;

        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "No transacton was loaded.");
            return poJSON;
        }

        if (lsStatus.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already confirmed.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(PaymentRequestStatus.CONFIRMED);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        //change status
        poJSON = statusChange(poMaster.getTable(), (String) poMaster.getValue("sTransNox"), remarks, lsStatus, !lbConfirm);

        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poJSON = new JSONObject();
        poJSON.put("result", "success");

        if (lbConfirm) {
            poJSON.put("message", "Transaction confirmed successfully.");
        } else {
            poJSON.put("message", "Transaction confirmation request submitted successfully.");
        }

        return poJSON;
    }

    public JSONObject PaidTransaction(String remarks) throws ParseException, SQLException, GuanzonException, CloneNotSupportedException {

        poJSON = new JSONObject();

        String lsStatus = PaymentRequestStatus.PAID;
        boolean lbConfirm = true;

        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "No transacton was loaded.");
            return poJSON;
        }

        if (lsStatus.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already Paid.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(PaymentRequestStatus.PAID);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        //change status
        poJSON = statusChange(poMaster.getTable(), (String) poMaster.getValue("sTransNox"), remarks, lsStatus, !lbConfirm);

        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poJSON = new JSONObject();
        poJSON.put("result", "success");

        if (lbConfirm) {
            poJSON.put("message", "Transaction Paid successfully.");
        } else {
            poJSON.put("message", "Transaction Paid request submitted successfully.");
        }

        return poJSON;
    }

    public JSONObject CancelTransaction(String remarks) throws ParseException, SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();

        String lsStatus = PaymentRequestStatus.CANCELLED;
        boolean lbConfirm = true;

        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "No transacton was loaded.");
            return poJSON;
        }

        if (lsStatus.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already cancelled.");
            return poJSON;
        }

        poJSON = isEntryOkay(PaymentRequestStatus.CANCELLED);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poJSON = statusChange(poMaster.getTable(), (String) poMaster.getValue("sTransNox"), remarks, lsStatus, !lbConfirm);

        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poJSON = new JSONObject();
        poJSON.put("result", "success");

        if (lbConfirm) {
            poJSON.put("message", "Transaction cancelled successfully.");
        } else {
            poJSON.put("message", "Transaction cancellation request submitted successfully.");
        }

        return poJSON;
    }

    public JSONObject VoidTransaction(String remarks) throws ParseException, SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();

        String lsStatus = PaymentRequestStatus.VOID;
        boolean lbConfirm = true;

        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "No transacton was loaded.");
            return poJSON;
        }

        if (lsStatus.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already voided.");
            return poJSON;
        }

        poJSON = isEntryOkay(PaymentRequestStatus.VOID);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poJSON = statusChange(poMaster.getTable(), (String) poMaster.getValue("sTransNox"), remarks, lsStatus, !lbConfirm);

        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poJSON = new JSONObject();
        poJSON.put("result", "success");

        if (lbConfirm) {
            poJSON.put("message", "Transaction voided successfully.");
        } else {
            poJSON.put("message", "Transaction voiding request submitted successfully.");
        }

        return poJSON;
    }

    public JSONObject PostTransaction(String remarks) throws ParseException, SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();

        String lsStatus = PaymentRequestStatus.POSTED;
        boolean lbConfirm = true;

        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "No transacton was loaded.");
            return poJSON;
        }

        if (lsStatus.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already processed.");
            return poJSON;
        }

        poJSON = isEntryOkay(PaymentRequestStatus.POSTED);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poJSON = statusChange(poMaster.getTable(), (String) poMaster.getValue("sTransNox"), remarks, lsStatus, !lbConfirm);

        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poJSON = new JSONObject();
        poJSON.put("result", "success");

        if (lbConfirm) {
            poJSON.put("message", "Transaction posted successfully.");
        } else {
            poJSON.put("message", "Transaction posting request submitted successfully.");
        }

        return poJSON;
    }

    public JSONObject ReturnTransaction(String remarks) throws ParseException, SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();

        String lsStatus = PaymentRequestStatus.RETURNED;
        boolean lbConfirm = true;

        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "No transacton was loaded.");
            return poJSON;
        }

        if (lsStatus.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already returned.");
            return poJSON;
        }

        poJSON = isEntryOkay(PaymentRequestStatus.RETURNED);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poJSON = statusChange(poMaster.getTable(), (String) poMaster.getValue("sTransNox"), remarks, lsStatus, !lbConfirm);

        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poJSON = new JSONObject();
        poJSON.put("result", "success");

        if (lbConfirm) {
            poJSON.put("message", "Transaction returned successfully.");
        } else {
            poJSON.put("message", "Transaction return request submitted successfully.");
        }

        return poJSON;
    }

    public JSONObject AddDetail() throws CloneNotSupportedException {
        if (Detail(getDetailCount() - 1).getParticularID().isEmpty()) {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "Last row has empty item.");
            return poJSON;
        }
        return addDetail();
    }

    private TransactionAttachment TransactionAttachment() throws SQLException, GuanzonException {
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
                if (Master().getEditMode() == EditMode.UPDATE) {
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

    @Override
    public void initSQL() {
        SQL_BROWSE = "SELECT "
                + " a.sTransNox,"
                + " a.dTransact,"
                + " b.sBranchNm,"
                + " c.sDeptName,"
                + " d.sPayeeNme"
                + " FROM payment_request_master a "
                + " LEFT JOIN Branch b ON a.sBranchCd = b.sBranchCd "
                + " LEFT JOIN Department c ON c.sDeptIDxx = a.sDeptIDxx "
                + " LEFT JOIN Payee d ON a.sPayeeIDx = d.sPayeeIDx";
    }

    public JSONObject SearchTransaction(String fsValue) throws CloneNotSupportedException, SQLException, GuanzonException {
        poJSON = new JSONObject();
        String lsTransStat = "";
        if (psTranStat.length() > 1) {
            for (int lnCtr = 0; lnCtr <= psTranStat.length() - 1; lnCtr++) {
                lsTransStat += ", " + SQLUtil.toSQL(Character.toString(psTranStat.charAt(lnCtr)));
            }
            lsTransStat = " AND a.cTranStat IN (" + lsTransStat.substring(2) + ")";
        } else {
            lsTransStat = " AND a.cTranStat = " + SQLUtil.toSQL(psTranStat);
        }
        initSQL();
        String lsFilterCondition = String.join(" AND ", "a.sPayeeIDx LIKE " + SQLUtil.toSQL("%" + Master().getPayeeID()),
                " b.sBranchCd = " + SQLUtil.toSQL(Master().getBranchCode()));
        String lsSQL = MiscUtil.addCondition(SQL_BROWSE, lsFilterCondition);
        if (!psTranStat.isEmpty()) {
            lsSQL = lsSQL + lsTransStat;
        }
        lsSQL = lsSQL + " GROUP BY a.sTransNox";
        System.out.println("SQL EXECUTED: " + lsSQL);
        poJSON = ShowDialogFX.Browse(poGRider,
                lsSQL,
                fsValue,
                "Transaction Date»Transaction No»Branch»Payee",
                "a.dTransact»a.sTransNox»b.sBranchNm»d.sPayeeNme",
                "a.dTransact»a.sTransNox»b.sBranchNm»d.sPayeeNme",
                1);

        if (poJSON != null) {
            return OpenTransaction((String) poJSON.get("sTransNox"));
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;
        }
    }

    public JSONObject SearchDepartment(String value, boolean byCode) throws ExceptionInInitializerError, SQLException, GuanzonException {
        Department object = new ParamControllers(poGRider, logwrapr).Department();
        object.setRecordStatus("1");

        poJSON = object.searchRecord(value, byCode);

        if ("success".equals((String) poJSON.get("result"))) {
            Master().setDepartmentID(object.getModel().getDepartmentId());
        }

        return poJSON;
    }

    public JSONObject SearchPayee(String value, boolean byCode) throws ExceptionInInitializerError, SQLException, GuanzonException {
        Payee object = new GLControllers(poGRider, logwrapr).Payee();
        object.setRecordStatus("1");

        poJSON = object.searchRecord(value, byCode);

        if ("success".equals((String) poJSON.get("result"))) {
            Master().setPayeeID(object.getModel().getPayeeID());
        }

        return poJSON;
    }

    public JSONObject SearchParticular(String value, boolean byCode, int row) throws ExceptionInInitializerError, SQLException, GuanzonException {
        Particular object = new GLControllers(poGRider, logwrapr).Particular();
        object.setRecordStatus("1");

        poJSON = object.searchRecord(value, Master().getPayeeID(), byCode);

        if ("success".equals((String) poJSON.get("result"))) {
            for (int lnRow = 0; lnRow <= getDetailCount() - 1; lnRow++) {
                if (lnRow != row) {
                    if ((Detail(lnRow).getParticularID().equals(object.getModel().getParticularID()))) {
                        poJSON.put("result", "error");
                        poJSON.put("message", "Particular: " + object.getModel().getDescription() + " already exist in table at row " + (lnRow + 1) + ".");
                        poJSON.put("tableRow", lnRow);
                        return poJSON;
                    }
                }
            }
            Detail(row).setParticularID(object.getModel().getParticularID());
        }

        return poJSON;
    }

    @Override
    public String getSourceCode() {
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
        
        Iterator<Model> detail = Detail().iterator();
                while (detail.hasNext()) {
                    Model item = detail.next(); // Store the item before checking conditions

                    Number amount = (Number) item.getValue("nAmountxx");
                    
                    if (amount.doubleValue() <= 0) {
                        detail.remove(); // Correctly remove the item
                    }
                }
        //assign other info on detail
        for (int lnCtr = 0; lnCtr <= getDetailCount() - 1; lnCtr++) {
            Detail(lnCtr).setTransactionNo(Master().getTransactionNo());
            Detail(lnCtr).setEntryNo(lnCtr + 1);
        }

        if (getDetailCount() == 1){
            //do not allow a single item detail with no quantity order
            if (Detail(0).getAmount().equals(0)) {
                poJSON.put("result", "error");
                poJSON.put("message", "Particular has 0 amount.");
                return poJSON;
            }
        }
        
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
        //Set Transaction Attachments
        for (int lnCtr = 0; lnCtr <= getTransactionAttachmentCount() - 1; lnCtr++) {
            TransactionAttachmentList(lnCtr).getModel().setSourceCode(SOURCE_CODE);
            TransactionAttachmentList(lnCtr).getModel().setSourceNo(Master().getTransactionNo());
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
        int lnCtr;
        
         //Save Attachments
            for (lnCtr = 0; lnCtr <= getTransactionAttachmentCount() - 1; lnCtr++) {
                if (paAttachments.get(lnCtr).getEditMode() == EditMode.ADDNEW || paAttachments.get(lnCtr).getEditMode() == EditMode.UPDATE) {
                    try {
                        paAttachments.get(lnCtr).setWithParentClass(true);
                        poJSON = paAttachments.get(lnCtr).saveRecord();
                        if ("error".equals((String) poJSON.get("result"))) {
                            return poJSON;
                        }
                    } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                        Logger.getLogger(PaymentRequest.class.getName()).log(Level.SEVERE, null, ex);
                    } 
                }
            }

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
    protected JSONObject isEntryOkay(String status) {
        GValidator loValidator = new PaymentRequestValidator();
        loValidator.setApplicationDriver(poGRider);
        loValidator.setTransactionStatus(status);
        poJSON = loValidator.validate();
        return poJSON;
    }

    private Model_Recurring_Issuance Recurring_IssuanceList() {
        return new GLModels(poGRider).Recurring_Issuance();
    }

    public Model_Recurring_Issuance Recurring_Issuance(int row) {
        return (Model_Recurring_Issuance) paRecurring.get(row);
    }

    public int getRecurring_IssuanceCount() {
        if(paRecurring == null){
            return 0;
        } 
            return paRecurring.size();
    }

    public JSONObject loadRecurringIssuance() throws SQLException, GuanzonException {
        JSONObject loJSON = new JSONObject();
        String lsSQL
                = "SELECT "
                + "  b.sBranchNm, "
                + "  a.sBranchCd, "
                + "  a.dBillDate, "
                + "  a.dDueUntil, "
                + "  a.sPrtclrID, "
                + "  e.sDescript, "
                + "  a.sPayeeIDx,"
                + "  a.sAcctNoxx "
                + " FROM "
                + "  recurring_issuance a "
                + "  LEFT JOIN branch b ON a.sBranchCd = b.sBranchCd "
                + "  LEFT JOIN payee c ON a.sPayeeIDx = c.sPayeeIDx "
                + "  LEFT JOIN client_master d ON c.sClientID = d.sClientID "
                + "  LEFT JOIN particular e ON a.sPrtclrID = e.sPrtclrID ";

        String lsFilterCondition = String.join(" AND ",
                " a.sBranchCd = " + SQLUtil.toSQL(Master().getBranchCode()),
                " a.sPayeeIDx LIKE " + SQLUtil.toSQL("%" + Master().getPayeeID()),
                " a.cRecdStat = " + SQLUtil.toSQL(Logical.YES));
        lsSQL = MiscUtil.addCondition(lsSQL, lsFilterCondition);

        System.out.println("Executing SQL: " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);

        int lnCtr = 0;
        if (MiscUtil.RecordCount(loRS) >= 0) {
            paRecurring = new ArrayList<>();
            while (loRS.next()) {
                // Print the result set
                System.out.println("sPrtclrID: " + loRS.getString("sPrtclrID"));
                System.out.println("sBranchCd: " + loRS.getString("sBranchCd"));
                System.out.println("sBranchCd: " + loRS.getString("sBranchCd"));
                System.out.println("------------------------------------------------------------------------------");

                paRecurring.add(Recurring_IssuanceList());
                paRecurring.get(paRecurring.size() - 1).openRecord(loRS.getString("sPrtclrID"),
                        loRS.getString("sBranchCd"),
                        loRS.getString("sPayeeIDx"),
                        loRS.getString("sAcctNoxx"));
                lnCtr++;
            }
            System.out.println("Records found: " + lnCtr);
            loJSON.put("result", "success");
            loJSON.put("message", "Record loaded successfully.");
        } else {
            paRecurring = new ArrayList<>();
            paRecurring.add(Recurring_IssuanceList());
            loJSON.put("result", "error");
            loJSON.put("continue", true);
            loJSON.put("message", "No record found .");
        }
        MiscUtil.close(loRS);
        return loJSON;
    }

    public JSONObject addRecurringIssuanceToPaymentRequestDetail(String particularNo, String payeeID, String AcctNo) throws CloneNotSupportedException, SQLException, GuanzonException {
        poJSON = new JSONObject();
        boolean lbExist = false;
        int lnRow = 0;
        RecurringIssuance poRecurringIssuance;
        poRecurringIssuance = new GLControllers(poGRider, logwrapr).RecurringIssuance();

        poJSON = poRecurringIssuance.openRecord(particularNo, Master().getBranchCode(), payeeID, AcctNo);
        if ("error".equals((String) poJSON.get("result"))) {
            poJSON.put("result", "error");
            return poJSON;
        }
        for (int lnCtr = 0; lnCtr <= getRecurring_IssuanceCount() - 1; lnCtr++) {
            //Check existing supplier
            if (Master().getPayeeID() == null || "".equals(Master().getPayeeID())) {
                Master().setPayeeID(poRecurringIssuance.getModel().getPayeeID());
            } else {
                if (!Master().getPayeeID().equals(poRecurringIssuance.getModel().getPayeeID())) {
                    if (getDetailCount() >= 0) {
                        poJSON.put("result", "error");
                        poJSON.put("message", "Payee must be equal to selected Recurring Issuance Payee.");
                        return poJSON;
                    } else {
                        Master().setPayeeID(poRecurringIssuance.getModel().getPayeeID());
                    }
                }
            }

            for (lnRow = 0; lnRow <= getDetailCount() - 1; lnRow++) {
                if (Detail(lnRow).getParticularID().equals(Recurring_Issuance(lnCtr).getParticularID())) {
                    lbExist = true;
                    break;
                }
            }

            if (!lbExist) {
                Detail(getDetailCount() - 1).setParticularID(Recurring_Issuance(lnCtr).getParticularID());
                Detail(getDetailCount() - 1).setAmount(Recurring_Issuance(lnCtr).getAmount());
                AddDetail();
            }
            lbExist = false;
        }
        poJSON.put("result", "success");
        return poJSON;
    }

    public JSONObject computeNetPayableDetails(double rent, boolean isVatExclusive, double vatRate, double wtaxRate) {
        JSONObject result = new JSONObject();
        double baseRent;
        double vat;
        double whtax;
        double total;
        double netPayable;

        if (isVatExclusive) {
            vat = rent * vatRate / (1 + vatRate);  // Extract VAT from total
            baseRent = rent - vat;
            whtax = baseRent * wtaxRate;
            total = rent;
            netPayable = total - whtax;
        } else {
            baseRent = rent;
            vat = rent * vatRate;
            total = rent + vat;
            whtax = rent * wtaxRate;
            netPayable = total - whtax;
        }

        result.put("baseRent", baseRent);
        result.put("vat", vat);
        result.put("wtax", whtax);
        result.put("total", total);
        result.put("netPayable", netPayable);
        result.put("result", "success");
        return result;
    }
    
    public JSONObject computeMasterFields() {
        poJSON = new JSONObject();
        double totalAmount = 0.00;
        double totalDiscountAmount = 0.00;
        double detailTaxAmount = 0.00;
        double detailNetAmount = 0.00;

        for (int lnCtr = 0; lnCtr <= getDetailCount() - 1; lnCtr++) {
            totalAmount += Detail(lnCtr).getAmount().doubleValue();
            totalDiscountAmount += Detail(lnCtr).getAddDiscount().doubleValue();
            if (Detail(lnCtr).getVatable().equals("1")) {
                poJSON = computeNetPayableDetails(Detail(lnCtr).getAmount().doubleValue() - Detail(lnCtr).getAddDiscount().doubleValue(), true, 0.12, 0.00);
            } else {
                poJSON = computeNetPayableDetails(Detail(lnCtr).getAmount().doubleValue() - Detail(lnCtr).getAddDiscount().doubleValue(), false, 0.12, 0.00);
            }
            detailTaxAmount += Double.parseDouble(poJSON.get("vat").toString());
            detailNetAmount += Double.parseDouble(poJSON.get("netPayable").toString());
        }

        Master().setTranTotal(totalAmount);
        Master().setDiscountAmount(totalDiscountAmount);
        Master().setTaxAmount(detailTaxAmount);
        Master().setNetTotal(detailNetAmount);
        return poJSON;
    }
    public JSONObject isDetailHasZeroAmount() {
        poJSON = new JSONObject();
        int zeroAmountRow = -1;
        boolean hasNonZeroAmount = false;
        boolean hasZeroAmount = false;
        int lastRow = getDetailCount() - 1;

        for (int lnRow = 0; lnRow <= lastRow; lnRow++) {
            double amount = Detail(lnRow).getAmount().doubleValue();
            String particularID = (String) Detail(lnRow).getValue("sPrtclrID");

            if (!particularID.isEmpty()) {
                if (amount == 0.00) {
                    hasZeroAmount = true;
                    if (zeroAmountRow == -1) {
                        zeroAmountRow = lnRow;
                    }
                } else {
                    hasNonZeroAmount = true;
                }
            }
        }

        if (!hasNonZeroAmount && hasZeroAmount) {
            poJSON.put("result", "error");
            poJSON.put("message", "All items have zero amount. Please enter a valid amount.");
            poJSON.put("tableRow", zeroAmountRow);
            poJSON.put("warning", "true");
        } else if (hasZeroAmount) {
            poJSON.put("result", "error");
            poJSON.put("message", "Some items have zero amount. Please review.");
            poJSON.put("tableRow", zeroAmountRow);
            poJSON.put("warning", "false");
        } else {
            poJSON.put("result", "success");
            poJSON.put("message", "All items have valid amounts.");
            poJSON.put("tableRow", lastRow);
        }

        return poJSON;
    }
    public void resetMaster() {
        poMaster = new GLModels(poGRider).PaymentRequestMaster();
    }

    public void resetOthers() {
        paAttachments = new ArrayList<>();
    }
}
