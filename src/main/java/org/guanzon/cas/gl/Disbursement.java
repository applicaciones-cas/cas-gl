package org.guanzon.cas.gl;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.agent.services.Transaction;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.RecordStatus;
import org.guanzon.appdriver.iface.GValidator;
import org.guanzon.cas.client.Client;
import org.guanzon.cas.client.services.ClientControllers;
import org.guanzon.cas.gl.model.Model_Check_Payments;
import org.guanzon.cas.gl.model.Model_Disbursement_Detail;
import org.guanzon.cas.gl.model.Model_Disbursement_Master;
import org.guanzon.cas.gl.services.GLControllers;
import org.guanzon.cas.gl.services.GLModels;
import org.guanzon.cas.gl.status.DisbursementStatic;
import org.guanzon.cas.gl.status.PaymentRequestStatus;
import org.guanzon.cas.gl.validator.DisbursementValidator;
import org.guanzon.cas.parameter.Banks;
import org.guanzon.cas.parameter.Branch;
import org.guanzon.cas.parameter.TaxCode;
import org.guanzon.cas.parameter.services.ParamControllers;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

public class Disbursement extends Transaction {

    List<Model_Disbursement_Master> poDisbursementMaster;
    private Model_Check_Payments poCheckPayments;
    private CheckPayments checkPayments;

    public JSONObject InitTransaction() throws SQLException, GuanzonException {
        SOURCE_CODE = "DISB";

        poMaster = new GLModels(poGRider).DisbursementMaster();
        poDetail = new GLModels(poGRider).DisbursementDetail();
        paDetail = new ArrayList<>();

        return initialize();
    }

    public JSONObject NewTransaction() throws CloneNotSupportedException {
        return newTransaction();
    }

    public JSONObject SaveTransaction() throws SQLException, GuanzonException, CloneNotSupportedException {
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

        String lsStatus = DisbursementStatic.VERIFIED;
        boolean lbConfirm = true;

        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "No transacton was loaded.");
            return poJSON;
        }

        if (lsStatus.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already Verified.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(DisbursementStatic.VERIFIED);
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
            poJSON.put("message", "Transaction Verified successfully.");
        } else {
            poJSON.put("message", "Transaction Verified request submitted successfully.");
        }

        return poJSON;
    }

    public JSONObject PostTransaction(String remarks) throws ParseException, SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();

        String lsStatus = DisbursementStatic.POSTED;
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

        //validator
        poJSON = isEntryOkay(DisbursementStatic.POSTED);
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
            poJSON.put("message", "Transaction posted successfully.");
        } else {
            poJSON.put("message", "Transaction posting request submitted successfully.");
        }

        return poJSON;
    }

    public JSONObject CancelTransaction(String remarks) throws ParseException, SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();

        String lsStatus = DisbursementStatic.CANCELLED;
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

        //validator
        poJSON = isEntryOkay(DisbursementStatic.CANCELLED);
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
            poJSON.put("message", "Transaction cancelled successfully.");
        } else {
            poJSON.put("message", "Transaction cancellation request submitted successfully.");
        }

        return poJSON;
    }

    public JSONObject VoidTransaction(String remarks) throws ParseException, SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();

        String lsStatus = DisbursementStatic.VOID;
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

        //validator
        poJSON = isEntryOkay(DisbursementStatic.VOID);
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
            poJSON.put("message", "Transaction voided successfully.");
        } else {
            poJSON.put("message", "Transaction voiding request submitted successfully.");
        }

        return poJSON;
    }

    public JSONObject AddDetail() throws CloneNotSupportedException {
        if (Detail(getDetailCount() - 1).getParticular().isEmpty()) {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "Last row has empty item.");
            return poJSON;
        }

        return addDetail();
    }

    /*Search Master References*/
    public JSONObject SearchBranch(String value, boolean byCode) throws ExceptionInInitializerError, SQLException, GuanzonException {
        Branch object = new ParamControllers(poGRider, logwrapr).Branch();
        object.setRecordStatus("1");

        poJSON = object.searchRecord(value, byCode);

        if ("success".equals((String) poJSON.get("result"))) {
            Master().setBranchCode(object.getModel().getBranchCode());
        }

        return poJSON;
    }

    public JSONObject SearchTaxCode(String value, int row, boolean byCode) throws ExceptionInInitializerError, SQLException, GuanzonException {
        TaxCode object = new ParamControllers(poGRider, logwrapr).TaxCode();
        object.setRecordStatus("1");

        poJSON = object.searchRecord(value, byCode);
        if ("success".equals((String) poJSON.get("result"))) {
            Detail(row).setTAxCode(object.getModel().getTaxCode());
        }

        return poJSON;
    }

    public JSONObject SearchSupplier(String value, boolean byCode) throws SQLException, GuanzonException {
        Client object = new ClientControllers(poGRider, logwrapr).Client();
        object.Master().setRecordStatus(RecordStatus.ACTIVE);
        object.Master().setClientType("1");
        poJSON = object.Master().searchRecord(value, byCode);

        if ("success".equals((String) poJSON.get("result"))) {

//            Master().setSupplierID(object.Master().getModel().getClientId());
//            Master().setAddressID(object.ClientAddress().getModel().getAddressId());
//            Master().setContactID(object.ClientInstitutionContact().getModel().getClientId());
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

    /*End - Search Master References*/
    @Override
    public String getSourceCode() {
        return SOURCE_CODE;
    }

    @Override
    public Model_Disbursement_Master Master() {
        return (Model_Disbursement_Master) poMaster;
    }
//
//    @Override
//    public Model_Disbursement_Detail Detail(int row) {
//        return (Model_Disbursement_Detail) paDetail.get(row);
//    }


    @Override
    public Model_Disbursement_Detail Detail(int row) {
         return (Model_Disbursement_Detail) paDetail.get(row);
//        if (row >= 0 && row < paDetail.size()) {
//            return (Model_Disbursement_Detail) paDetail.get(row);
//        } else {
//            throw new IndexOutOfBoundsException("Invalid row index: " + row + ", size: " + paDetail.size());
//        }
    }
    public CheckPayments CheckPayments() throws SQLException, GuanzonException {
        if (Master().getOldDisbursementType().equals(DisbursementStatic.DisbursementType.CHECK)
                || Master().getDisbursementType().equals(DisbursementStatic.DisbursementType.CHECK)) {
            // Only initialize if null, or you want to force recreate each time
            if (checkPayments == null) {
                checkPayments = new GLControllers(poGRider, logwrapr).CheckPayments();
                checkPayments.setWithParentClass(true);

                if (Master().getEditMode() == EditMode.ADDNEW) {
                    checkPayments.newRecord();

                    CheckPayments().getModel().setSourceNo(Master().getTransactionNo());
                    CheckPayments().getModel().setSourceCode(SOURCE_CODE);
                } else if (Master().getEditMode() == EditMode.UPDATE || Master().getEditMode() == EditMode.READY) {
                    String transactionNo = Master().getTransactionNo();
                    String checkPaymentTransactionNo = checkPayments.getTransactionNoOfCheckPayment(transactionNo, SOURCE_CODE);

                    checkPayments.openRecord(checkPaymentTransactionNo);

                    if (Master().getEditMode() == EditMode.UPDATE) {
                        checkPayments.updateRecord();
                        

                        boolean disbursementTypeChanged = !Master().getDisbursementType().equals(Master().getOldDisbursementType());
                        if (disbursementTypeChanged) {
                                    //change status

                            if(Master().getDisbursementType().equals(DisbursementStatic.DisbursementType.CHECK)){
                              checkPayments.getModel().setTransactionStatus(DisbursementStatic.OPEN);
                            } else{
                             checkPayments.getModel().setTransactionStatus(DisbursementStatic.VOID);
                            }
                        }
                    }

                    checkPayments.getModel().setSourceNo(transactionNo);
                    checkPayments.getModel().setSourceCode(SOURCE_CODE);
                }
            }
        }
        return checkPayments;
    }

    public JSONObject saveCheckPayments() throws SQLException, GuanzonException, CloneNotSupportedException {
        System.out.println("checkPayments save: " + checkPayments.getEditMode());
        if ("error".equals(checkPayments.saveRecord().get("result"))) {
            poJSON.put("result", "error");
            return poJSON;
        }
        poJSON.put("result", "success");
        return poJSON;
    }

    @Override
    public JSONObject willSave() throws SQLException, GuanzonException {
        /*Put system validations and other assignments here*/
        poJSON = new JSONObject();

        //remove items with no stockid or quantity order       
        Iterator<Model> detail = Detail().iterator();
        while (detail.hasNext()) {
            Model item = detail.next(); // Store the item before checking conditions

            if ("".equals((String) item.getValue("sSourceNo"))
                    || (double) item.getValue("nAmountxx") <= 0) {
                detail.remove(); // Correctly remove the item
            }
        }

        //assign other info on detail
        for (int lnCtr = 0; lnCtr <= getDetailCount() - 1; lnCtr++) {
            Detail(lnCtr).setTransactionNo(Master().getTransactionNo());
            Detail(lnCtr).setEntryNo(lnCtr + 1);
        }
//        
        if (getDetailCount() == 1){
            //do not allow a single item detail with no quantity order
            if (Detail(0).getAmount().doubleValue()== 0.0000) {
                poJSON.put("result", "error");
                poJSON.put("message", "Your order has zero quantity.");
                return poJSON;
            }
        }

        poJSON.put("result", "success");
        return poJSON;
    }

    @Override
    public JSONObject save() {
        /*Put saving business rules here*/
        return isEntryOkay(DisbursementStatic.OPEN);
    }

    @Override
    public JSONObject saveOthers() {
        try {
            /*Only modify this if there are other tables to modify except the master and detail tables*/

            if (Master().getOldDisbursementType().equals(DisbursementStatic.DisbursementType.CHECK)
                    || Master().getDisbursementType().equals(DisbursementStatic.DisbursementType.CHECK)) {
                poJSON = new JSONObject();
                poJSON = saveCheckPayments();
                if ("error".equals(poJSON.get("result"))) {
                    poGRider.rollbackTrans();
                    return poJSON;
                }
            }
        } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
            Logger.getLogger(Disbursement.class.getName()).log(Level.SEVERE, null, ex);
        }
        poJSON.put("result", "success");
        return poJSON;
    }

    @Override
    public void saveComplete() {
        /*This procedure was called when saving was complete*/
        System.out.println("Transaction saved successfully.");
    }

    @Override
    public JSONObject initFields() {
        /*Put initial model values here*/
        poJSON = new JSONObject();

        poJSON.put("result", "success");
        return poJSON;
    }

    @Override
    public void initSQL() {
        SQL_BROWSE = "SELECT "
                + " a.sTransNox,"
                + " a.dTransact,"
                + " c.sBranchNm,"
                + " d.sPayeeNme,"
                + " e.sCompnyNm AS supplier,"
                + " f.sParticular,"
                + " a.nNetTotal "
                + " FROM Disbursement_Master a "
                + " JOIN Disbursement_Detail b ON a.sTransNox = b.sTransNox "
                + " JOIN Branch c ON a.sBranchCd = c.sBranchCd "
                + " JOIN Payee d ON a.sPayeeIDx = d.sPayeeIDx "
                + " JOIN client_master e ON d.sClientID = e.sClientID "
                + " JOIN particular f ON b.sPrtclrID = f.sPrtclrID";
    }

    @Override
    protected JSONObject isEntryOkay(String status) {
        GValidator loValidator = new DisbursementValidator();
        loValidator.setApplicationDriver(poGRider);
        loValidator.setTransactionStatus(status);
        loValidator.setMaster(Master());
        poJSON = loValidator.validate();
        return poJSON;
    }

    public JSONObject getUnifiedPayments() throws SQLException, GuanzonException {

        String lsSQL = "SELECT * FROM ("
                + "    SELECT "
                + "        a.sTransNox, "
                + "        a.dTransact, "
                + "        (a.`nTranTotl` - a.`nAmtPaidx`) as Balance, "
                + "        'AP' AS TransactionType, "
                + "        'AP_Payment_Master' AS SourceTable "
                + "    FROM AP_Payment_Master a "
                + "    WHERE a.cTranStat =  '" + PaymentRequestStatus.CONFIRMED + "' "
                + "      AND (a.nTranTotl - a.nAmtPaidx) > " + DisbursementStatic.DefaultValues.default_value_double
                + "    UNION ALL "
                + "    SELECT "
                + "        b.sTransNox, "
                + "        b.dTransact, "
                + "        (b.`nNetTotal` - b.`nAmtPaidx`) as Balance, "
                + "        'PRF' AS TransactionType, "
                + "        'Payment_Request_Master' AS SourceTable "
                + "    FROM Payment_Request_Master b "
                + "    WHERE b.cTranStat = '" + PaymentRequestStatus.CONFIRMED + "' "
                + "      AND (b.nNetTotal - b.nAmtPaidx) > " + DisbursementStatic.DefaultValues.default_value_double
                + "    UNION ALL "
                + "    SELECT "
                + "        c.sTransNox, "
                + "        c.dTransact, "
                + "        (c.`nGrossAmt` - c.`nAmtPaidx`) as Balance, "
                + "        'CP' AS TransactionType, "
                + "        'Cache_Payables_Master' AS SourceTable "
                + "    FROM Cache_Payables_Master c "
                + "    WHERE c.cStatusxx =  '" + PaymentRequestStatus.CONFIRMED + "' "
                + "       AND (c.nGrossAmt - c.nAmtPaidx) > " + DisbursementStatic.DefaultValues.default_value_double
                + ") AS CombinedResults "
                + "ORDER BY dTransact ASC ";

        System.out.println("Executing SQL: " + lsSQL);

        ResultSet loRS = poGRider.executeQuery(lsSQL);
        JSONArray dataArray = new JSONArray();
        JSONObject loJSON = new JSONObject();

        if (loRS == null) {
            loJSON.put("result", "error");
            loJSON.put("message", "Query execution failed.");
            return loJSON;
        }

        try {
            int lnctr = 0;

            while (loRS.next()) {
                JSONObject record = new JSONObject();
                record.put("sTransNox", loRS.getString("sTransNox"));
                record.put("dTransact", loRS.getDate("dTransact"));
                record.put("Balance", loRS.getDouble("Balance"));  // Assuming amount is decimal/double
                record.put("TransactionType", loRS.getString("TransactionType"));

                dataArray.add(record);
                lnctr++;
            }

            if (lnctr > 0) {
                loJSON.put("result", "success");
                loJSON.put("message", "Record(s) loaded successfully.");
                loJSON.put("data", dataArray);
            } else {
                loJSON.put("result", "error");
                loJSON.put("message", "No records found.");
                loJSON.put("data", new JSONArray());
            }

            MiscUtil.close(loRS);

        } catch (SQLException e) {
            loJSON.put("result", "error");
            loJSON.put("message", e.getMessage());
        }

        return loJSON;
    }

    public JSONObject addUnifiedPaymentToDisbursement(String transactionNo, String paymentType)
            throws CloneNotSupportedException, SQLException, GuanzonException {

        JSONObject poJSON = new JSONObject();
        int detailCount = 0;

        PaymentRequest poPaymentRequest = null;
//    APPaymentMaster poAPPaymentMaster = null;
//    CachePayableMaster poCachePayableMaster = null;

        switch (paymentType) {
            case "PRF":
                poPaymentRequest = new GLControllers(poGRider, logwrapr).PaymentRequest();
                poJSON = poPaymentRequest.InitTransaction();
                if (!"success".equals(poJSON.get("result"))) {
                    poJSON.put("message", "No records found.");
                    return poJSON;
                }
                poJSON = poPaymentRequest.OpenTransaction(transactionNo);
                if (!"success".equals(poJSON.get("result"))) {
                    poJSON.put("message", "No records found.");
                    return poJSON;
                }
                detailCount = poPaymentRequest.getDetailCount();
                break;

            case "SOA":
//            poAPPaymentMaster = new GLControllers(poGRider, logwrapr).APPaymentMaster();
//            poJSON = poAPPaymentMaster.InitTransaction();
//            if (!"success".equals(poJSON.get("result"))) {
//                poJSON.put("message", "No records found.");
//                return poJSON;
//            }
//            poJSON = poAPPaymentMaster.OpenTransaction(transactionNo);
//            if (!"success".equals(poJSON.get("result"))) {
//                poJSON.put("message", "No records found.");
//                return poJSON;
//            }
//            detailCount = poAPPaymentMaster.getDetailCount();
                break;

            case "CP":
//            poCachePayableMaster = new GLControllers(poGRider, logwrapr).CachePayableMaster();
//            poJSON = poCachePayableMaster.InitTransaction();
//            if (!"success".equals(poJSON.get("result"))) {
//                poJSON.put("message", "No records found.");
//                return poJSON;
//            }
//            poJSON = poCachePayableMaster.OpenTransaction(transactionNo);
//            if (!"success".equals(poJSON.get("result"))) {
//                poJSON.put("message", "No records found.");
//                return poJSON;
//            }
//            detailCount = poCachePayableMaster.getDetailCount();
                break;

            default:
                poJSON.put("result", "error");
                poJSON.put("message", "Invalid payment type.");
                return poJSON;
        }

        // Loop through details outside switch
        for (int lnCtr = 0; lnCtr < detailCount; lnCtr++) {
            String sourceNo = "";
            String sourceCode = "";
            String accountCode = "";
            double amount = 0.0000;

            // Extract details based on paymentType
            switch (paymentType) {
                case "PRF":

                    sourceNo = poPaymentRequest.Detail(lnCtr).getTransactionNo();
                    sourceCode = DisbursementStatic.SourceCode.PAYMENT_REQUEST;
                    accountCode = poPaymentRequest.Detail(lnCtr).Particular().getAccountCode();
                    amount = (double) poPaymentRequest.Detail(lnCtr).getAmount().doubleValue();
                    break;

                case "SOA":
//                sourceNo = poAPPaymentMaster.Detail(lnCtr).getTransactionNo();
//                sourceCode = poAPPaymentMaster.Detail(lnCtr).getTransactionNo();
//                accountCode = poAPPaymentMaster.Detail(lnCtr).Particular().getAccountCode();
//                amount = (double) poAPPaymentMaster.Detail(lnCtr).getAmount();
                    break;

                case "CP":
//                sourceNo = poCachePayableMaster.Detail(lnCtr).getTransactionNo();
//                sourceCode = poCachePayableMaster.Detail(lnCtr).getTransactionNo();
//                accountCode = poCachePayableMaster.Detail(lnCtr).Particular().getAccountCode();
//                amount = (double) poCachePayableMaster.Detail(lnCtr).getAmount();
                    break;
            }

            boolean found = false;
            for (int i = 0; i < detailCount - 1 ; i++) {
                if (Detail(i).getSourceNo().equals(sourceNo)
                        && Detail(i).getSourceCode().equals(sourceCode)) {

                    double currentAmount = 0.0000;
                    try {
                        currentAmount = (double) Detail(i).getAmount().doubleValue();
                    } catch (NumberFormatException e) {
                        currentAmount = 0.0000;
                    }

                    Detail(i).setAmount(currentAmount + amount);
                    found = true;
                    break;
                }
            }

            if (!found) {
                int lnLastIndex = detailCount - 1 ;
                Detail(lnLastIndex).setSourceNo(sourceNo);
                Detail(lnLastIndex).setSourceCode(sourceCode);
                Detail(lnLastIndex).setAccountCode(accountCode);
                Detail(lnLastIndex).setAmount(amount);                
                AddDetail();
            }
        }

        poJSON.put("result", "success");
        poJSON.put("message", "Record loaded successfully.");
        return poJSON;
    }

//    public JSONObject addUnifiedPaymentToDisbursement(String transactionNo, String paymentType) 
//        throws CloneNotSupportedException, SQLException, GuanzonException {
//
//    poJSON = new JSONObject();
//
//    PaymentRequest poPaymentRequest = null;
////    APPaymentMaster poAPPaymentMaster = null;
////    CachePayableMaster poCachePayableMaster = null;
//
//    int detailCount = 0;
//
//    switch (paymentType) {
//        case "PRF":
//            poPaymentRequest = new GLControllers(poGRider, logwrapr).PaymentRequest();
//            poJSON = poPaymentRequest.InitTransaction();
//            if (!"success".equals(poJSON.get("result"))) {
//                poJSON.put("message", "No records found.");
//                return poJSON;
//            }
//            poJSON = poPaymentRequest.OpenTransaction(transactionNo);
//            if (!"success".equals(poJSON.get("result"))) {
//                poJSON.put("message", "No records found.");
//                return poJSON;
//            }
//            detailCount = poPaymentRequest.getDetailCount();
//            break;
//
//        case "SOA":
////            poAPPaymentMaster = new GLControllers(poGRider, logwrapr).APPaymentMaster();
////            poJSON = poAPPaymentMaster.InitTransaction();
////            if (!"success".equals(poJSON.get("result"))) {
////                poJSON.put("message", "No records found.");
////                return poJSON;
////            }
////            poJSON = poAPPaymentMaster.OpenTransaction(transactionNo);
////            if (!"success".equals(poJSON.get("result"))) {
////                poJSON.put("message", "No records found.");
////                return poJSON;
////            }
////            detailCount = poAPPaymentMaster.getDetailCount();
//            break;
//
//        case "CP":
////            poCachePayableMaster = new GLControllers(poGRider, logwrapr).CachePayableMaster();
////            poJSON = poCachePayableMaster.InitTransaction();
////            if (!"success".equals(poJSON.get("result"))) {
////                poJSON.put("message", "No records found.");
////                return poJSON;
////            }
////            poJSON = poCachePayableMaster.OpenTransaction(transactionNo);
////            if (!"success".equals(poJSON.get("result"))) {
////                poJSON.put("message", "No records found.");
////                return poJSON;
////            }
////            detailCount = poCachePayableMaster.getDetailCount();
//            break;
//
//        default:
//              poJSON.put("result", "error");
//              return poJSON;
//    }
//
//    // Now loop through details and add them properly
//    for (int lnCtr = 0; lnCtr < detailCount; lnCtr++) {
//        
//        int lnLastIndex = detailCount - 1;
//
//        switch (paymentType) {
//                
//            case "PRF":
//                Detail(lnLastIndex).setSourceNo(poPaymentRequest.Detail(lnCtr).getTransactionNo());
//                Detail(lnLastIndex).setSourceCode(poPaymentRequest.Detail(lnCtr).getTransactionNo());
////                Detail(lnLastIndex).setparticular(poPaymentRequest.Detail(lnCtr).getParticularID());
//                Detail(lnLastIndex).setAccountCode(poPaymentRequest.Detail(lnCtr).Particular().getAccountCode());
//                Detail(lnLastIndex).setAmount(poPaymentRequest.Detail(lnCtr).getAmount());
//                break;
//
//            case "SOA":
////                Detail(lnLastIndex).setSourceNo(poAPPaymentMaster.Detail(lnCtr).getTransactionNo());
////                Detail(lnLastIndex).setSourceCode(poAPPaymentMaster.Detail(lnCtr).getTransactionNo());
////                Detail(lnLastIndex).setparticular(poAPPaymentMaster.Detail(lnCtr).getParticularID());
////                Detail(lnLastIndex).setAccountCode(poAPPaymentMaster.Detail(lnCtr).Particular().getAccountCode());
////                Detail(lnLastIndex).setAmount(poAPPaymentMaster.Detail(lnCtr).getAmount());
//                break;
//
//            case "CP":
////                Detail(lnLastIndex).setSourceNo(poCachePayableMaster.Detail(lnCtr).getTransactionNo());
////                Detail(lnLastIndex).setSourceCode(poCachePayableMaster.Detail(lnCtr).getTransactionNo());
////                Detail(lnLastIndex).setparticular(poCachePayableMaster.Detail(lnCtr).getParticularID());
////                Detail(lnLastIndex).setAccountCode(poCachePayableMaster.Detail(lnCtr).Particular().getAccountCode());
////                Detail(lnLastIndex).setAmount(poCachePayableMaster.Detail(lnCtr).getAmount());
//                break;
//        }
//        AddDetail();
//    }
//    poJSON.put("result", "success");
//    poJSON.put("message", "Record loaded successfully.");
//    return poJSON;
//}
//    
    public JSONObject getDisbursement(String fsTransactionNo, String fsPayee) throws SQLException, GuanzonException {
        JSONObject loJSON = new JSONObject();
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

        String lsFilterCondition = String.join(" AND ", "a.sPayeeIDx LIKE " + SQLUtil.toSQL("%" + fsPayee),
                " a.sTransNox  LIKE " + SQLUtil.toSQL("%" + fsTransactionNo),
                " ");
        String lsSQL = MiscUtil.addCondition(SQL_BROWSE, lsFilterCondition);

        lsSQL = MiscUtil.addCondition(lsSQL, lsFilterCondition);
        if (!psTranStat.isEmpty()) {
            lsSQL = lsSQL + lsTransStat;
        }
        lsSQL = lsSQL + " GROUP BY  a.sTransNox"
                + " ORDER BY dTransact ASC";
        System.out.println("Executing SQL: " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);

        int lnCtr = 0;
        if (MiscUtil.RecordCount(loRS) >= 0) {
            poDisbursementMaster = new ArrayList<>();
            while (loRS.next()) {
                // Print the result set
                System.out.println("sTransNox: " + loRS.getString("sTransNox"));
                System.out.println("dTransact: " + loRS.getDate("dTransact"));
                System.out.println("------------------------------------------------------------------------------");

                poDisbursementMaster.add(DisbursementMasterList());
                poDisbursementMaster.get(poDisbursementMaster.size() - 1).openRecord(loRS.getString("sTransNox"));
                lnCtr++;
            }
            System.out.println("Records found: " + lnCtr);
            loJSON.put("result", "success");
            loJSON.put("message", "Record loaded successfully.");
        } else {
            poDisbursementMaster = new ArrayList<>();
            poDisbursementMaster.add(DisbursementMasterList());
            loJSON.put("result", "error");
            loJSON.put("continue", true);
            loJSON.put("message", "No record found .");
        }
        MiscUtil.close(loRS);
        return loJSON;
    }

    private Model_Disbursement_Master DisbursementMasterList() {
        return new GLModels(poGRider).DisbursementMaster();
    }

    public int getDisbursementMasterCount() {
        return this.poDisbursementMaster.size();
    }

    public Model_Disbursement_Master poDisbursementMaster(int row) {
        return (Model_Disbursement_Master) poDisbursementMaster.get(row);
    }

    public void exportDisbursementMasterMetadataToXML(String filePath) throws SQLException, IOException {
        String query = "SELECT"
                + "  sTransNox"
                + ", sBranchCD"
                + ", dTransact"
                + ", sBankIDxx"
                + ", sBnkActID"
                + ", sCheckNox"
                + ", dCheckDte"
                + ", sPayorIDx"
                + ", sPayeeIDx"
                + ", nAmountxx"
                + ", sRemarksx"
                + ", sSourceCd"
                + ", sSourceNo"
                + ", cLocation"
                + ", cIsReplcd"
                + ", cReleased"
                + ", cPayeeTyp"
                + ", cDisbMode"
                + ", cClaimant"
                + ", sAuthorze"
                + ", cIsCrossx"
                + ", cIsPayeex"
                + ", cTranStat"
                + ", sModified"
                + ", dModified"
                + ", dTimeStmp"
                + " FROM check_payments";

        ResultSet rs = poGRider.executeQuery(query);

        if (rs == null) {
            throw new SQLException("Failed to execute query.");
        }

        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<metadata>\n");
        xml.append("  <table>Check_Payments</table>\n");

        for (int i = 1; i <= columnCount; i++) {
            xml.append("  <column>\n");
            xml.append("    <COLUMN_NAME>").append(metaData.getColumnName(i)).append("</COLUMN_NAME>\n");
            xml.append("    <COLUMN_LABEL>").append(metaData.getColumnLabel(i)).append("</COLUMN_LABEL>\n");
            xml.append("    <DATA_TYPE>").append(metaData.getColumnType(i)).append("</DATA_TYPE>\n");
            xml.append("    <NULLABLE>").append(metaData.isNullable(i) == ResultSetMetaData.columnNullable ? 1 : 0).append("</NULLABLE>\n");
            xml.append("    <LENGTH>").append(metaData.getColumnDisplaySize(i)).append("</LENGTH>\n");
            xml.append("    <PRECISION>").append(metaData.getPrecision(i)).append("</PRECISION>\n");
            xml.append("    <SCALE>").append(metaData.getScale(i)).append("</SCALE>\n");
            xml.append("    <FORMAT>null</FORMAT>\n");
            xml.append("    <REGTYPE>null</REGTYPE>\n");
            xml.append("    <FROM>null</FROM>\n");
            xml.append("    <THRU>null</THRU>\n");
            xml.append("    <LIST>null</LIST>\n");
            xml.append("  </column>\n");
        }

        xml.append("</metadata>");

        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(xml.toString());
        }

        MiscUtil.close(rs);
    }

}
