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
import org.guanzon.appdriver.agent.ShowDialogFX;
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
import org.guanzon.cas.gl.services.SOATaggingControllers;
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
    List<PaymentRequest> poPaymentRequest;
    List<SOATagging> poApPayments;
    List<Model> paDetailRemoved;

    public JSONObject InitTransaction() throws SQLException, GuanzonException {
        SOURCE_CODE = "DISB";

        poMaster = new GLModels(poGRider).DisbursementMaster();
        poDetail = new GLModels(poGRider).DisbursementDetail();
        checkPayments = new GLControllers(poGRider,logwrapr).CheckPayments();
        paDetail = new ArrayList<>();
        poPaymentRequest = new ArrayList<>();
        poApPayments = new ArrayList<>();

        return initialize();
    }

    public JSONObject NewTransaction() throws CloneNotSupportedException {
        return newTransaction();
    }

    public JSONObject SaveTransaction() throws SQLException, GuanzonException, CloneNotSupportedException {
        return saveTransaction();
    }

    public JSONObject OpenTransaction(String transactionNo) throws CloneNotSupportedException, SQLException, GuanzonException {
//        resetMaster();
//        Detail().clear();
        return openTransaction(transactionNo);
    }

    public JSONObject UpdateTransaction() {
        return updateTransaction();
    }

    public JSONObject VerifyTransaction(String remarks) throws ParseException, SQLException, GuanzonException, CloneNotSupportedException {
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
            Detail(row).setTaxRates(object.getModel().getRegularRate());
            Detail(row).setTaxAmount((Detail(row).getAmount().doubleValue() * object.getModel().getRegularRate() / 100));
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

    public JSONObject SearchTransaction(String fsValue) throws CloneNotSupportedException, SQLException, GuanzonException {
        poJSON = new JSONObject();
        String lsTransStat = "";
        String lsBranch = "";
        if (psTranStat.length() > 1) {
            for (int lnCtr = 0; lnCtr <= psTranStat.length() - 1; lnCtr++) {
                lsTransStat += ", " + SQLUtil.toSQL(Character.toString(psTranStat.charAt(lnCtr)));
            }
            lsTransStat = " AND a.cTranStat IN (" + lsTransStat.substring(2) + ")";
        } else {
            lsTransStat = " AND a.cTranStat = " + SQLUtil.toSQL(psTranStat);
        }

        initSQL();
        String lsFilterCondition = String.join(" AND ", "a.sIndstCdx = " + SQLUtil.toSQL(Master().getIndustryID()),
                " a.sCompnyID = " + SQLUtil.toSQL(Master().getCompanyID()));
//                " a.sSupplier LIKE " + SQLUtil.toSQL("%" + fsSupplierID),
//                " f.sCategrCd LIKE " + SQLUtil.toSQL("%" + Master().getCategoryCode()),
//                " a.sTransNox LIKE " + SQLUtil.toSQL("%" + fsReferID));

        String lsSQL = MiscUtil.addCondition(SQL_BROWSE, lsFilterCondition);
        if (!psTranStat.isEmpty()) {
            lsSQL = lsSQL + lsTransStat;
        }

        lsSQL = lsSQL + " GROUP BY a.sTransNox";
        System.out.println("SQL EXECUTED xxx : " + lsSQL);
        poJSON = ShowDialogFX.Browse(poGRider,
                lsSQL,
                fsValue,
                "Transaction No»Transaction Date»Branch»Supplier",
                "a.sTransNox»a.dTransact»c.sBranchNm»supplier",
                "a.sTransNox»a.dTransact»IFNULL(c.sBranchNm, '')»IFNULL(e.sCompnyNm, '')",
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

    public JSONObject SearchBanks(String value, boolean byCode) throws ExceptionInInitializerError, SQLException, GuanzonException {
        Banks object = new ParamControllers(poGRider, logwrapr).Banks();
        object.setRecordStatus("1");

        poJSON = object.searchRecord(value, byCode);

        if ("success".equals((String) poJSON.get("result"))) {
            CheckPayments().getModel().setBankID(object.getModel().getBankID());
            System.out.println("search result == " +    CheckPayments().getModel().getBankID()); 
            System.out.println("search result == " +    CheckPayments().getModel().Banks().getBankName());
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

    @Override
    public Model_Disbursement_Detail Detail(int row) {
        return (Model_Disbursement_Detail) paDetail.get(row);
    }
    

    public CheckPayments CheckPayments() {
        return (CheckPayments)checkPayments;
    }
    
    public JSONObject setCheckpayment() throws GuanzonException, SQLException{
        if (Master().getOldDisbursementType().equals(DisbursementStatic.DisbursementType.CHECK)
                || Master().getDisbursementType().equals(DisbursementStatic.DisbursementType.CHECK)) {
            // Only initialize if null, or you want to force recreate each time
            int editMode = Master().getEditMode();
            String transactionNo = Master().getTransactionNo();
            String checkPaymentTransactionNo = "";
            if (checkPayments == null) {
                checkPayments = new GLControllers(poGRider, logwrapr).CheckPayments();
                checkPayments.setWithParentClass(true);
            }

                switch (editMode) {
                    case EditMode.ADDNEW:
                        if (checkPayments.getEditMode()!= EditMode.ADDNEW){
                            checkPayments.newRecord();
                            System.out.println("CHECK set to new ==== " +checkPayments.getEditMode() );
                            checkPayments.getModel().setSourceNo(Master().getTransactionNo());
                            checkPayments.getModel().setSourceCode(SOURCE_CODE);
                        }
                        break;
                    case EditMode.READY:
                        if (checkPayments.getEditMode()!= EditMode.READY) {
                            checkPaymentTransactionNo = checkPayments.getTransactionNoOfCheckPayment(transactionNo, SOURCE_CODE);
                            checkPayments.openRecord(checkPaymentTransactionNo);
                        }
                        break;

                    case EditMode.UPDATE:
                        if (checkPayments.getEditMode()!= EditMode.UPDATE ) {
                            checkPaymentTransactionNo = checkPayments.getTransactionNoOfCheckPayment(transactionNo, SOURCE_CODE);
                            checkPayments.openRecord(checkPaymentTransactionNo);
                            checkPayments.updateRecord();
                            boolean disbursementTypeChanged = !Master().getDisbursementType().equals(Master().getOldDisbursementType());
                            if (disbursementTypeChanged) {
                                if (Master().getDisbursementType().equals(DisbursementStatic.DisbursementType.CHECK)) {
                                    checkPayments.getModel().setTransactionStatus(DisbursementStatic.OPEN);
                                } else {
                                    checkPayments.getModel().setTransactionStatus(DisbursementStatic.VOID);
                                }
                            }
                        }
                        break;
                }
            }
        System.out.println("CHECK EDIT MODE ==== " +checkPayments.getEditMode() );
        poJSON.put("result", "success");
        return poJSON;
    }
    
//    public CheckPayments CheckPayments() throws SQLException, GuanzonException {
//        if (Master().getOldDisbursementType().equals(DisbursementStatic.DisbursementType.CHECK)
//                || Master().getDisbursementType().equals(DisbursementStatic.DisbursementType.CHECK)) {
//            // Only initialize if null, or you want to force recreate each time
//            if (checkPayments == null) {
//                checkPayments = new GLControllers(poGRider, logwrapr).CheckPayments();
//                checkPayments.setWithParentClass(true);
//
//                if (Master().getEditMode() == EditMode.ADDNEW) {
//                    checkPayments.newRecord();
//
//                    CheckPayments().getModel().setSourceNo(Master().getTransactionNo());
//                    CheckPayments().getModel().setSourceCode(SOURCE_CODE);
//                } else if (Master().getEditMode() == EditMode.UPDATE || Master().getEditMode() == EditMode.READY) {
//                    String transactionNo = Master().getTransactionNo();
//                    String checkPaymentTransactionNo = checkPayments.getTransactionNoOfCheckPayment(transactionNo, SOURCE_CODE);
//
//                    checkPayments.openRecord(checkPaymentTransactionNo);
//
//                    if (Master().getEditMode() == EditMode.UPDATE) {
//                        checkPayments.updateRecord();
//
//                        boolean disbursementTypeChanged = !Master().getDisbursementType().equals(Master().getOldDisbursementType());
//                        if (disbursementTypeChanged) {
//                            if (Master().getDisbursementType().equals(DisbursementStatic.DisbursementType.CHECK)) {
//                                checkPayments.getModel().setTransactionStatus(DisbursementStatic.OPEN);
//                            } else {
//                                checkPayments.getModel().setTransactionStatus(DisbursementStatic.VOID);
//                            }
//                        }
//                    }
//
//                    checkPayments.getModel().setSourceNo(transactionNo);
//                    checkPayments.getModel().setSourceCode(SOURCE_CODE);
//                }
//            }
//        }
//        return checkPayments;
//    }

    public JSONObject saveCheckPayments() throws SQLException, GuanzonException, CloneNotSupportedException {
//        System.out.println("checkPayments save: " + checkPayments.getEditMode());
        if ("error".equals(checkPayments.saveRecord().get("result"))) {
            poJSON.put("result", "error");
            return poJSON;
        }
        poJSON.put("result", "success");
        return poJSON;
    }

    private PaymentRequest PaymentRequest() throws SQLException, GuanzonException {
        return new GLControllers(poGRider, logwrapr).PaymentRequest();
    }

    private SOATagging SOATagging() throws SQLException, GuanzonException {
        return new SOATaggingControllers(poGRider, logwrapr).SOATagging();
    }

    public JSONObject updateDisbursementsSource(String sourceNo, String sourceCode, String particular, Boolean isAdd)
            throws SQLException, GuanzonException, CloneNotSupportedException {
        int lnRow, lnList;
        boolean lbExist = false;

        switch (sourceCode) {
            case "PRF":
                for (lnRow = 0; lnRow <= poPaymentRequest.size() - 1; lnRow++) {
                    if (poPaymentRequest.get(lnRow).Master().getTransactionNo() != null) {
                        if (sourceNo.equals(poPaymentRequest.get(lnRow).Master().getTransactionNo())) {
                            lbExist = true;
                            break;
                        }
                    }
                }
                if (!lbExist) {
                    poPaymentRequest.add(PaymentRequest());
                    poPaymentRequest.get(poPaymentRequest.size() - 1).InitTransaction();
                    poPaymentRequest.get(poPaymentRequest.size() - 1).OpenTransaction(sourceNo);
                    poPaymentRequest.get(poPaymentRequest.size() - 1).UpdateTransaction();
                    lnList = poPaymentRequest.size() - 1;
                } else {
                    lnList = lnRow;
                }
                if (isAdd) {
                    poPaymentRequest.get(lnList).Master().setProcess(DisbursementStatic.DefaultValues.default_value_string_1);
                    poPaymentRequest.get(lnList).Master().setModifyingId(poGRider.getUserID());
                    poPaymentRequest.get(lnList).Master().setModifiedDate(poGRider.getServerDate());
                } else {
                    poPaymentRequest.get(lnList).Master().setProcess(DisbursementStatic.DefaultValues.default_value_string);
                    poPaymentRequest.get(lnList).Master().setModifyingId(poGRider.getUserID());
                    poPaymentRequest.get(lnList).Master().setModifiedDate(poGRider.getServerDate());
                }
                break;

            case "SOA":
                for (lnRow = 0; lnRow <= poApPayments.size() - 1; lnRow++) {
                    if (poApPayments.get(lnRow).Master().getTransactionNo() != null) {
                        if (sourceNo.equals(poApPayments.get(lnRow).Master().getTransactionNo())) {
                            lbExist = true;
                            break;
                        }
                    }
                }
                if (!lbExist) {
                    poApPayments.add(SOATagging());
                    poApPayments.get(poApPayments.size() - 1).InitTransaction();
                    poApPayments.get(poApPayments.size() - 1).OpenTransaction(sourceNo);
                    poApPayments.get(poApPayments.size() - 1).UpdateTransaction();
                    lnList = poApPayments.size() - 1;
                } else {
                    lnList = lnRow;
                }
                if (isAdd) {
                    poApPayments.get(lnList).Master().isProcessed(false);
                    poApPayments.get(lnList).Master().setModifyingId(poGRider.getUserID());
                    poApPayments.get(lnList).Master().setModifiedDate(poGRider.getServerDate());
                } else {
                    poApPayments.get(lnList).Master().isProcessed(false);
                    poApPayments.get(lnList).Master().setModifyingId(poGRider.getUserID());
                    poApPayments.get(lnList).Master().setModifiedDate(poGRider.getServerDate());
                }
                break;

            case "CP":
//              OBject is not available
                break;

            default:
                poJSON.put("result", "error");
                poJSON.put("message", "Invalid payment type.");
                return poJSON;
        }

        poJSON.put("result", "success");
        return poJSON;
    }

    private JSONObject saveDisbursementsSource()
            throws CloneNotSupportedException, SQLException, GuanzonException {
        poJSON = new JSONObject();
        int lnCtr, lnCtr1;
        for (lnCtr = 0; lnCtr <= poPaymentRequest.size() - 1; lnCtr++) {
            poPaymentRequest.get(lnCtr).setWithParent(true);
            poJSON = poPaymentRequest.get(lnCtr).SaveTransaction();
            if ("error".equals((String) poJSON.get("result"))) {
                return poJSON;
            }
        }
        for (lnCtr1 = 0; lnCtr1 <= poApPayments.size() - 1; lnCtr1++) {
            poApPayments.get(lnCtr1).setWithParent(true);
            poJSON = poApPayments.get(lnCtr1).SaveTransaction();
            if ("error".equals((String) poJSON.get("result"))) {
                return poJSON;
            }
        }

        poJSON.put("result", "success");
        return poJSON;
    }

    @Override
    public JSONObject willSave() throws SQLException, GuanzonException, CloneNotSupportedException {
        String sourceNo = "";
        String sourceCode = "";
        String particular = "";
        /*Put system validations and other assignments here*/
        poJSON = new JSONObject();
        if (paDetailRemoved == null) {
            paDetailRemoved = new ArrayList<>();
        }
        //remove items with no stockid or quantity order

        Iterator<Model> detail = Detail().iterator();
        while (detail.hasNext()) {
            Model item = detail.next(); // Store the item before checking conditions
            sourceNo = (String) item.getValue("sSourceNo");
            Number amount = (Number) item.getValue("nAmountxx");

            if (amount.doubleValue() <= 0 || "".equals(sourceNo)) {
                detail.remove(); // Correctly remove the item
                if (Master().getEditMode() == EditMode.UPDATE) {
                    paDetailRemoved.add(item);
                }
            }
        }
        if (getDetailCount() == 1) {
            //do not allow a single item detail with no quantity order
            if (Detail(0).getAmount().doubleValue() == 0.0000) {
                poJSON.put("result", "error");
                poJSON.put("message", "Your order has zero quantity.");
                return poJSON;
            }
        }

        poJSON = setValueToOthers();
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        for (int lnCtr = 0; lnCtr <= getDetailCount() - 1; lnCtr++) {
            Detail(lnCtr).setTransactionNo(Master().getTransactionNo());
            Detail(lnCtr).setEntryNo(lnCtr + 1);
        }

        poJSON.put("result", "success");
        return poJSON;
    }

    public int getDetailRemovedCount() {
        if (paDetailRemoved == null) {
            paDetailRemoved = new ArrayList<>();
        }

        return paDetailRemoved.size();
    }

    public Model_Disbursement_Detail DetailRemove(int row) {
        return (Model_Disbursement_Detail) paDetailRemoved.get(row);
    }

    private JSONObject setValueToOthers()
            throws CloneNotSupportedException,
            SQLException,
            GuanzonException {
        poJSON = new JSONObject();
        poPaymentRequest = new ArrayList<>();
        int lnCtr;

        //Update Purchase Order exist in PO Receiving Detail
        for (lnCtr = 0; lnCtr <= getDetailCount() - 1; lnCtr++) {
            System.out.println("----------------------PURCHASE ORDER RECEIVING DETAIL---------------------- ");
            System.out.println("TransNo : " + (lnCtr + 1) + " : " + Detail(lnCtr).getTransactionNo());
            System.out.println("sourceno : " + (lnCtr + 1) + " : " + Detail(lnCtr).getSourceNo());
            System.out.println("sourceCode : " + (lnCtr + 1) + " : " + Detail(lnCtr).getSourceCode());
            System.out.println("particular : " + (lnCtr + 1) + " : " + Detail(lnCtr).getParticular());
            System.out.println("------------------------------------------------------------------ ");

            updateDisbursementsSource(Detail(lnCtr).getSourceNo(), Detail(lnCtr).getSourceCode(), Detail(lnCtr).getParticular(), true);
        }
        //Update stock request removed
        for (lnCtr = 0; lnCtr <= getDetailRemovedCount() - 1; lnCtr++) {
            //Purchase Order
            updateDisbursementsSource(DetailRemove(lnCtr).getSourceNo(), DetailRemove(lnCtr).getSourceCode(), DetailRemove(lnCtr).getParticular(), false);
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

            poJSON = saveDisbursementsSource();
            if ("error".equals(poJSON.get("result"))) {
                poGRider.rollbackTrans();
                return poJSON;
            }

            if (Master().getOldDisbursementType().equals(DisbursementStatic.DisbursementType.CHECK)
                    || Master().getDisbursementType().equals(DisbursementStatic.DisbursementType.CHECK)) {
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
    public void initSQL() {
        SQL_BROWSE = "SELECT "
                + " a.sTransNox,"
                + " a.dTransact,"
                + " c.sBranchNm,"
                + " d.sPayeeNme,"
                + " e.sCompnyNm AS supplier,"
                + " f.sDescript,"
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

    public JSONObject getUnifiedPayments(String Trantype) throws SQLException, GuanzonException {
        StringBuilder lsSQL = new StringBuilder("SELECT * FROM (");
        boolean hasCondition = false;

        if (DisbursementStatic.SourceCode.LOAD_ALL.equals(Trantype) || DisbursementStatic.SourceCode.ACCOUNTS_PAYABLE.equals(Trantype)) {
            if (hasCondition) {
                lsSQL.append(" UNION ALL ");
            }
            lsSQL.append(
                    "SELECT "
                    + "    a.sTransNox, "
                    + "    a.dTransact, "
                    + "    (a.nTranTotl - a.nAmtPaidx) AS Balance, "
                    + "    'SOA' AS TransactionType, "
                    + "    'AP_Payment_Master' AS SourceTable, "
                    + "    a.sIndstCdx AS Industry, "
                    + "    a.sCompnyID AS Company "
                    + "FROM AP_Payment_Master a "
                    + "WHERE a.cTranStat = '" + PaymentRequestStatus.CONFIRMED + "' "
                    + "  AND (a.nTranTotl - a.nAmtPaidx) > " + DisbursementStatic.DefaultValues.default_value_double_0000 + " "
                    + "  AND a.cProcessd = '" + PaymentRequestStatus.OPEN + "' "
                    + "  AND a.sIndstCdx = '" + Master().getIndustryID() + "' "
                    + "  AND a.sCompnyID = '" + Master().getCompanyID() + "'"
            );
            hasCondition = true;
        }

        if (DisbursementStatic.SourceCode.LOAD_ALL.equals(Trantype) || DisbursementStatic.SourceCode.PAYMENT_REQUEST.equals(Trantype)) {
            if (hasCondition) {
                lsSQL.append(" UNION ALL ");
            }
            lsSQL.append(
                    "SELECT "
                    + "    b.sTransNox, "
                    + "    b.dTransact, "
                    + "    (b.nNetTotal - b.nAmtPaidx) AS Balance, "
                    + "    'PRF' AS TransactionType, "
                    + "    'Payment_Request_Master' AS SourceTable, "
                    + "    b.sIndstCdx AS Industry, "
                    + "    b.sCompnyID AS Company "
                    + "FROM Payment_Request_Master b "
                    + "WHERE b.cTranStat = '" + PaymentRequestStatus.CONFIRMED + "' "
                    + "  AND (b.nNetTotal - b.nAmtPaidx) > " + DisbursementStatic.DefaultValues.default_value_double_0000 + " "
                    + "  AND b.cProcessd = '" + PaymentRequestStatus.OPEN + "' "
                    + "  AND b.sIndstCdx = '" + Master().getIndustryID() + "' "
                    + "  AND b.sCompnyID = '" + Master().getCompanyID() + "'"
            );
            hasCondition = true;
        }

        if (DisbursementStatic.SourceCode.LOAD_ALL.equals(Trantype) || DisbursementStatic.SourceCode.CASH_PAYABLE.equals(Trantype)) {
            if (hasCondition) {
                lsSQL.append(" UNION ALL ");
            }
            lsSQL.append(
                    "SELECT "
                    + "    c.sTransNox, "
                    + "    c.dTransact, "
                    + "    (c.nGrossAmt - c.nAmtPaidx) AS Balance, "
                    + "    'CP' AS TransactionType, "
                    + "    'Cache_Payable_Master' AS SourceTable, "
                    + "    c.sIndstCdx AS Industry, "
                    + "    c.sCompnyID AS Company "
                    + "FROM Cache_Payable_Master c "
                    + "WHERE c.cTranStat = '" + PaymentRequestStatus.CONFIRMED + "' "
                    + "  AND (c.nGrossAmt - c.nAmtPaidx) > " + DisbursementStatic.DefaultValues.default_value_double + " "
                    + "  AND c.cProcessd = '" + PaymentRequestStatus.OPEN + "' "
                    + "  AND c.sIndstCdx = '" + Master().getIndustryID() + "' "
                    + "  AND c.sCompnyID = '" + Master().getCompanyID() + "'"
            );
            hasCondition = true;
        }

        lsSQL.append(") AS CombinedResults ORDER BY dTransact ASC");

        System.out.println("Executing SQL: " + lsSQL.toString());

        ResultSet loRS = poGRider.executeQuery(lsSQL.toString());
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
                record.put("Balance", loRS.getDouble("Balance"));
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

        int detailCount = 0;
        PaymentRequest poPaymentRequest = new GLControllers(poGRider, logwrapr).PaymentRequest();
        JSONObject poJSON;

        switch (paymentType) {
            case "PRF":
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
            case "CP":
                poJSON = new JSONObject();
                poJSON.put("result", "error");
                poJSON.put("message", "Unsupported payment type.");
                return poJSON;

            default:
                poJSON = new JSONObject();
                poJSON.put("result", "error");
                poJSON.put("message", "Invalid payment type.");
                return poJSON;
        }

        // Loop through PaymentRequest details
        for (int lnCtr = 0; lnCtr < detailCount; lnCtr++) {
            String sourceNo = "";
            String sourceCode = "";
            String particular = "";
            double amount = 0.0000;

            // Only PRF supported
            sourceNo = poPaymentRequest.Detail(lnCtr).getTransactionNo();
            sourceCode = DisbursementStatic.SourceCode.PAYMENT_REQUEST;
            particular = poPaymentRequest.Detail(lnCtr).getParticularID();
            amount = poPaymentRequest.Detail(lnCtr).getAmount().doubleValue();

            boolean found = false;
            int disbursementDetailCount = getDetailCount();

            for (int i = 0; i < disbursementDetailCount; i++) {
                if (Detail(i).getSourceNo().equals(sourceNo)
                        && Detail(i).getSourceCode().equals(sourceCode)
                        && Detail(i).getParticular().equals(particular)) {

                    double currentAmount = 0.0000;
                    try {
                        currentAmount = Detail(i).getAmount().doubleValue();
                    } catch (NumberFormatException e) {
                        currentAmount = 0.0000;
                    }

                    Detail(i).setAmount(currentAmount + amount);
                    found = true;
                    break;
                }
            }

            if (!found) {
                AddDetail(); // Adds a new detail entry
                int newIndex = getDetailCount() - 1;

                Detail(newIndex).setSourceNo(sourceNo);
                Detail(newIndex).setSourceCode(sourceCode);
                Detail(newIndex).setParticular(particular);
                Detail(newIndex).setAmount(amount);
            }
        }

        poJSON.put("result", "success");
        poJSON.put("message", "Record loaded successfully.");
        return poJSON;
    }

    public JSONObject getDisbursement(String fsTransactionNo, String fsPayee) throws SQLException, GuanzonException {
        poJSON = new JSONObject();
        // Build transaction status condition
        String lsTransStat = "";
        if (psTranStat.length() > 1) {
            for (int lnCtr = 0; lnCtr < psTranStat.length(); lnCtr++) {
                lsTransStat += ", " + SQLUtil.toSQL(Character.toString(psTranStat.charAt(lnCtr)));
            }
            lsTransStat = " AND a.cTranStat IN (" + lsTransStat.substring(2) + ")";
        } else if (!psTranStat.isEmpty()) {
            lsTransStat = " AND a.cTranStat = " + SQLUtil.toSQL(psTranStat);
        }

        initSQL();

        // Filter conditions (empty string means show all)
        String lsFilterCondition = "a.sPayeeIDx LIKE " + SQLUtil.toSQL("%" + fsPayee + "%")
                + " AND a.sTransNox LIKE " + SQLUtil.toSQL("%" + fsTransactionNo + "%");

        // Start from base SQL and apply filters
        String lsSQL = MiscUtil.addCondition(SQL_BROWSE, lsFilterCondition);

        // Add transaction status condition
        if (!lsTransStat.isEmpty()) {
            lsSQL += lsTransStat;
        }

        // Grouping and sorting
        lsSQL += " GROUP BY a.sTransNox ORDER BY a.dTransact ASC";

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
            poJSON.put("result", "success");
            poJSON.put("message", "Record loaded successfully.");
        } else {
            poDisbursementMaster = new ArrayList<>();
            poDisbursementMaster.add(DisbursementMasterList());
            poJSON.put("result", "error");
            poJSON.put("continue", true);
            poJSON.put("message", "No record found .");
        }
        MiscUtil.close(loRS);
        return poJSON;
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

    public JSONObject computeFields() {
        poJSON = new JSONObject();
        double lnTotalVatSales = 0.0000;
        double lnTotalVatRates = 0.00;
        double lnTotalVatAmount = 0.0000;
        double lnTotalVatZeroRatedSales = 0.0000;
        double lnTotalVatExemptSales = 0.0000;
        double lnTotalPurchaseAmount = 0.0000;
        double lnLessWithHoldingTax = 0.0000;
        for (int lnCntr = 0; lnCntr <= getDetailCount() - 1; lnCntr++) {
            lnTotalVatRates += Detail(lnCntr).getTaxRates().doubleValue();
            lnTotalPurchaseAmount += Detail(lnCntr).getAmount().doubleValue();
            lnTotalVatAmount += (Detail(lnCntr).getAmount().doubleValue() * (Detail(lnCntr).getTaxRates().doubleValue() / 100));
        }
        if (lnTotalPurchaseAmount - lnTotalVatAmount <= 0.0000) {
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid Net Total Amount.");
            return poJSON;
        }
        Master().setVATRates(lnTotalVatRates);
        Master().setTransactionTotal(lnTotalPurchaseAmount);
        Master().setNetTotal(lnTotalPurchaseAmount - lnTotalVatAmount);
        poJSON.put("result", "success");
        poJSON.put("message", "computed successfully");
        return poJSON;
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

    public void resetMaster() {
        poMaster = new GLModels(poGRider).DisbursementMaster();
    }

    @Override
    public JSONObject initFields() {
        //Put initial model values here/
        poJSON = new JSONObject();
        try {
            //Put initial model values here/
            poJSON = new JSONObject();
            Master().setBranchCode(poGRider.getBranchCode());
            Master().setIndustryID(psIndustryId);
            Master().setCompanyID(psCompanyId);
            Master().setTransactionDate(poGRider.getServerDate());
            Master().setTransactionStatus(DisbursementStatic.OPEN);

        } catch (SQLException ex) {
            Logger.getLogger(Disbursement.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            poJSON.put("result", "error");
            poJSON.put("message", MiscUtil.getException(ex));
            return poJSON;
        }
        poJSON.put("result", "success");
        return poJSON;
    }
    private String psIndustryId = "";
    private String psCompanyId = "";

    public void setIndustryID(String industryID) {
        psIndustryId = industryID;
    }

    public void setCompanyID(String companyID) {
        psCompanyId = companyID;
    }
}
