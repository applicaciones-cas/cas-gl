/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.guanzon.cas.gl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.guanzon.appdriver.agent.ShowDialogFX;
import org.guanzon.appdriver.agent.services.Parameter;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.Logical;
import org.guanzon.appdriver.constant.RecordStatus;
import org.guanzon.appdriver.constant.UserRight;
import org.guanzon.appdriver.iface.GValidator;
import org.guanzon.cas.client.Client;
import org.guanzon.cas.client.services.ClientControllers;
import org.guanzon.cas.gl.model.Model_AP_Payment_Adjustment;
import org.guanzon.cas.gl.services.GLControllers;
import org.guanzon.cas.gl.services.GLModels;
import org.guanzon.cas.gl.status.APPaymentAdjustmentStatus;
import org.guanzon.cas.gl.validator.APPaymentAdjustmentValidator;
import org.guanzon.cas.parameter.Company;
import org.guanzon.cas.parameter.services.ParamControllers;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

/**
 *
 * @author Arsiela 06052025
 */
public class APPaymentAdjustment extends Parameter {

    private String psIndustryId = "";
    private String psCompanyId = "";

    Model_AP_Payment_Adjustment poModel;
    List<Model_AP_Payment_Adjustment> paModel;

    @Override
    public void initialize() {
        psRecdStat = Logical.YES;
        pbInitRec = true;

        poModel = new GLModels(poGRider).APPaymentAdjustment();

        paModel = new ArrayList<>();
    }

    public JSONObject NewTransaction()
            throws CloneNotSupportedException, SQLException, GuanzonException {
        return newRecord();
    }

    public JSONObject SaveTransaction()
            throws SQLException,
            GuanzonException,
            CloneNotSupportedException {
        return saveRecord();
    }

    public JSONObject OpenTransaction(String transactionNo)
            throws CloneNotSupportedException,
            SQLException,
            GuanzonException {
        return openRecord(transactionNo);
    }

    public JSONObject UpdateTransaction() {
        return updateRecord();
    }

    public JSONObject ConfirmTransaction(String remarks)
            throws ParseException,
            SQLException,
            GuanzonException,
            CloneNotSupportedException {
        poJSON = new JSONObject();

        String lsStatus = APPaymentAdjustmentStatus.CONFIRMED;
        boolean lbConfirm = true;

        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "No transacton was loaded.");
            return poJSON;
        }

        if (lsStatus.equals((String) poModel.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already confirmed.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(APPaymentAdjustmentStatus.CONFIRMED);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        if (poGRider.getUserLevel() == UserRight.ENCODER) {
            poJSON = ShowDialogFX.getUserApproval(poGRider);
            if (!"success".equals((String) poJSON.get("result"))) {
                return poJSON;
            }
        }
        
        poJSON = UpdateTransaction();
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        poModel.setTransactionStatus(APPaymentAdjustmentStatus.CONFIRMED);
        
        poJSON = SaveTransaction();
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

//        poGRider.beginTrans("UPDATE STATUS", "ConfirmTransaction", SOURCE_CODE, poModel.getTransactionNo());
//
//        //change status
////        poJSON = statusChange(poModel.getTable(), (String) poModel.getValue("sTransNox"), remarks, lsStatus, !lbConfirm, true);
//        if (!"success".equals((String) poJSON.get("result"))) {
//            poGRider.rollbackTrans();
//            return poJSON;
//        }
//
//        //Create Cache_Payables TODO
//        poGRider.commitTrans();

        poJSON = new JSONObject();
        poJSON.put("result", "success");
        if (lbConfirm) {
            poJSON.put("message", "Transaction confirmed successfully.");
        } else {
            poJSON.put("message", "Transaction confirmation request submitted successfully.");
        }

        return poJSON;
    }

    public JSONObject ReturnTransaction(String remarks)
            throws ParseException,
            SQLException,
            GuanzonException,
            CloneNotSupportedException {
        poJSON = new JSONObject();

        String lsStatus = APPaymentAdjustmentStatus.RETURNED;
        boolean lbReturn = true;

        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "No transacton was loaded.");
            return poJSON;
        }

        if (lsStatus.equals((String) poModel.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already returned.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(APPaymentAdjustmentStatus.RETURNED);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        if (APPaymentAdjustmentStatus.CONFIRMED.equals(poModel.getTransactionStatus())) {
            if (poGRider.getUserLevel() == UserRight.ENCODER) {
                poJSON = ShowDialogFX.getUserApproval(poGRider);
                if (!"success".equals((String) poJSON.get("result"))) {
                    return poJSON;
                }
            }
        }
        
        poJSON = UpdateTransaction();
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        poModel.setTransactionStatus(APPaymentAdjustmentStatus.RETURNED);
        
        poJSON = SaveTransaction();
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

//        poGRider.beginTrans("UPDATE STATUS", "ReturnTransaction", SOURCE_CODE, poModel.getTransactionNo());
//
//        //change status
////        poJSON = statusChange(poModel.getTable(), (String) poModel.getValue("sTransNox"), remarks, lsStatus, !lbReturn, true);
//        if (!"success".equals((String) poJSON.get("result"))) {
//            poGRider.rollbackTrans();
//            return poJSON;
//        }
//
//        //Update Cache Payables?
//        poGRider.commitTrans();

        poJSON = new JSONObject();
        poJSON.put("result", "success");
        if (lbReturn) {
            poJSON.put("message", "Transaction returned successfully.");
        } else {
            poJSON.put("message", "Transaction return request submitted successfully.");
        }

        return poJSON;
    }

    public JSONObject PaidTransaction(String remarks)
            throws ParseException,
            SQLException,
            GuanzonException,
            CloneNotSupportedException {
        poJSON = new JSONObject();

        String lsStatus = APPaymentAdjustmentStatus.PAID;
        boolean lbPaid = true;

        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "No transacton was loaded.");
            return poJSON;
        }

        if (lsStatus.equals((String) poModel.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already paid.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(APPaymentAdjustmentStatus.PAID);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        poJSON = UpdateTransaction();
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        poModel.setTransactionStatus(APPaymentAdjustmentStatus.PAID);
        poModel.isProcessed(true);
        
        poJSON = SaveTransaction();
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
//
//        poGRider.beginTrans("UPDATE STATUS", "PaidTransaction", SOURCE_CODE, poModel.getTransactionNo());
//
//        //change status
////        poJSON = statusChange(poModel.getTable(), (String) poModel.getValue("sTransNox"), remarks, lsStatus, !lbPaid, true);
//        if (!"success".equals((String) poJSON.get("result"))) {
//            poGRider.rollbackTrans();
//            return poJSON;
//        }

        //Update Cache Payables?
//        poJSON = poModel.openRecord(poModel.getTransactionNo());
//        if (!"success".equals((String) poJSON.get("result"))) {
//            poGRider.rollbackTrans();
//            return poJSON;
//        }

//        poJSON = poModel.updateRecord();
//        if (!"success".equals((String) poJSON.get("result"))) {
//            poGRider.rollbackTrans();
//            return poJSON;
//        }
//
//        poModel.isProcessed(true);
//
//        poJSON = poModel.saveRecord();
//        if (!"success".equals((String) poJSON.get("result"))) {
//            poGRider.rollbackTrans();
//            return poJSON;
//        }
//
//        poGRider.commitTrans();

        poJSON = new JSONObject();
        poJSON.put("result", "success");
        if (lbPaid) {
            poJSON.put("message", "Transaction paid successfully.");
        } else {
            poJSON.put("message", "Transaction paid request submitted successfully.");
        }

        return poJSON;
    }

    public JSONObject CancelTransaction(String remarks)
            throws ParseException,
            SQLException,
            GuanzonException,
            CloneNotSupportedException {
        poJSON = new JSONObject();

        String lsStatus = APPaymentAdjustmentStatus.CANCELLED;
        boolean lbCancelled = true;

        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "No transacton was loaded.");
            return poJSON;
        }

        if (lsStatus.equals((String) poModel.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already cancelled.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(APPaymentAdjustmentStatus.CANCELLED);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        if (APPaymentAdjustmentStatus.CONFIRMED.equals(poModel.getTransactionStatus())) {
            if (poGRider.getUserLevel() == UserRight.ENCODER) {
                poJSON = ShowDialogFX.getUserApproval(poGRider);
                if (!"success".equals((String) poJSON.get("result"))) {
                    return poJSON;
                }
            }
        }
        
        poJSON = UpdateTransaction();
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        poModel.setTransactionStatus(APPaymentAdjustmentStatus.CANCELLED);
        
        poJSON = SaveTransaction();
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

//        poGRider.beginTrans("UPDATE STATUS", "CancelTransaction", SOURCE_CODE, poModel.getTransactionNo());
//
//        //change status
////        poJSON = statusChange(poModel.getTable(), (String) poModel.getValue("sTransNox"), remarks, lsStatus, !lbCancelled, true);
//        if (!"success".equals((String) poJSON.get("result"))) {
//            poGRider.rollbackTrans();
//            return poJSON;
//        }
//
//        if (APPaymentAdjustmentStatus.CONFIRMED.equals(poModel.getTransactionStatus())) {
//            //Update Cache Payables
//
//        }
//
//        poGRider.commitTrans();

        poJSON = new JSONObject();
        poJSON.put("result", "success");
        if (lbCancelled) {
            poJSON.put("message", "Transaction cancelled successfully.");
        } else {
            poJSON.put("message", "Transaction cancellation request submitted successfully.");
        }

        return poJSON;
    }

    public JSONObject VoidTransaction(String remarks)
            throws ParseException,
            SQLException,
            GuanzonException,
            CloneNotSupportedException {
        poJSON = new JSONObject();

        String lsStatus = APPaymentAdjustmentStatus.VOID;
        boolean lbVoid = true;

        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "No transacton was loaded.");
            return poJSON;
        }

        if (lsStatus.equals((String) poModel.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already voided.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(APPaymentAdjustmentStatus.VOID);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        if (APPaymentAdjustmentStatus.CONFIRMED.equals(poModel.getTransactionStatus())) {
            if (poGRider.getUserLevel() == UserRight.ENCODER) {
                poJSON = ShowDialogFX.getUserApproval(poGRider);
                if (!"success".equals((String) poJSON.get("result"))) {
                    return poJSON;
                }
            }
        }
        
        poJSON = UpdateTransaction();
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        poModel.setTransactionStatus(APPaymentAdjustmentStatus.VOID);
        
        poJSON = SaveTransaction();
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

//        poGRider.beginTrans("UPDATE STATUS", "VoidTransaction", SOURCE_CODE, poModel.getTransactionNo());
//
//        //change status
////        poJSON = statusChange(poModel.getTable(), (String) poModel.getValue("sTransNox"), remarks, lsStatus, !lbVoid, true);
//        if (!"success".equals((String) poJSON.get("result"))) {
//            poGRider.rollbackTrans();
//            return poJSON;
//        }
//
//        if (APPaymentAdjustmentStatus.CONFIRMED.equals(poModel.getTransactionStatus())) {
//            //Update Cache Payables
//        }
//
//        poGRider.commitTrans();

        poJSON = new JSONObject();
        poJSON.put("result", "success");
        if (lbVoid) {
            poJSON.put("message", "Transaction voided successfully.");
        } else {
            poJSON.put("message", "Transaction voiding request submitted successfully.");
        }

        return poJSON;
    }

    public void setIndustryId(String industryId) {
        psIndustryId = industryId;
    }

    public void setCompanyId(String companyId) {
        psCompanyId = companyId;
    }

    @Override
    public JSONObject initFields() {
        try {
            /*Put initial model values here*/
            poJSON = new JSONObject();
            poModel.setBranchCode(poGRider.getBranchCode());
            poModel.setIndustryId(psIndustryId);
            poModel.setCompanyId(psCompanyId);
            poModel.setTransactionDate(poGRider.getServerDate());
            poModel.setTransactionStatus(APPaymentAdjustmentStatus.OPEN);
            poModel.isProcessed(false);

        } catch (SQLException ex) {
            Logger.getLogger(APPaymentAdjustment.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            poJSON.put("result", "error");
            poJSON.put("message", MiscUtil.getException(ex));
            return poJSON;
        }

        poJSON.put("result", "success");
        return poJSON;
    }

    public JSONObject computeFields() {
        poJSON = new JSONObject();

        poJSON.put("result", "success");
        return poJSON;
    }

    public JSONObject SaveRecord() {
        poJSON = new JSONObject();

        poJSON.put("result", "success");
        return poJSON;
    }

    private Model_AP_Payment_Adjustment APPaymentAdjustment() {
        return new GLModels(poGRider).APPaymentAdjustment();
    }

    public Model_AP_Payment_Adjustment APPaymentAdjustmentList(int row) {
        return (Model_AP_Payment_Adjustment) paModel.get(row);
    }

    public int getAPPaymentAdjustmentCount() {
        return this.paModel.size();
    }

    public JSONObject loadAPPaymentAdjustment(String companyId, String supplierId, String referenceNo) {
        poJSON = new JSONObject();
        try {
            if (companyId == null) {
                companyId = "";
            }
            if (supplierId == null) {
                supplierId = "";
            }
            if (referenceNo == null) {
                referenceNo = "";
            }

            String lsTransStat = "";
            if (psRecdStat != null) {
                if (psRecdStat.length() > 1) {
                    for (int lnCtr = 0; lnCtr <= psRecdStat.length() - 1; lnCtr++) {
                        lsTransStat += ", " + SQLUtil.toSQL(Character.toString(psRecdStat.charAt(lnCtr)));
                    }
                    lsTransStat = " AND a.cTranStat IN (" + lsTransStat.substring(2) + ")";
                } else {
                    lsTransStat = " AND a.cTranStat = " + SQLUtil.toSQL(psRecdStat);
                }
            }

            String lsSQL = MiscUtil.addCondition(getSQ_Browse(), //" a.sIndstCdx = " + SQLUtil.toSQL(psIndustryId)
                    " a.sCompnyID LIKE " + SQLUtil.toSQL("%" + companyId)
                    + " AND a.sClientID LIKE " + SQLUtil.toSQL("%" + supplierId)
                    + " AND a.sTransNox LIKE " + SQLUtil.toSQL("%" + referenceNo)
            );

            lsSQL = lsSQL + "" + lsTransStat + " ORDER BY a.dTransact DESC ";

            System.out.println("Executing SQL: " + lsSQL);
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            poJSON = new JSONObject();

            int lnctr = 0;

            if (MiscUtil.RecordCount(loRS) >= 0) {
                paModel = new ArrayList<>();
                while (loRS.next()) {
                    // Print the result set
                    System.out.println("sTransNox: " + loRS.getString("sTransNox"));
                    System.out.println("dTransact: " + loRS.getDate("dTransact"));
                    System.out.println("sCompnyNm: " + loRS.getString("sCompnyNm"));
                    System.out.println("------------------------------------------------------------------------------");

                    paModel.add(APPaymentAdjustment());
                    paModel.get(paModel.size() - 1).openRecord(loRS.getString("sTransNox"));
                    lnctr++;
                }

                System.out.println("Records found: " + lnctr);
                poJSON.put("result", "success");
                poJSON.put("message", "Record loaded successfully.");
            } else {
                paModel = new ArrayList<>();
                paModel.add(APPaymentAdjustment());
                poJSON.put("result", "error");
                poJSON.put("continue", true);
                poJSON.put("message", "No record found.");
            }
            MiscUtil.close(loRS);
        } catch (SQLException e) {
            poJSON.put("result", "error");
            poJSON.put("message", e.getMessage());
        } catch (GuanzonException ex) {
            Logger.getLogger(APPaymentAdjustment.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            poJSON.put("result", "error");
            poJSON.put("message", MiscUtil.getException(ex));
        }
        poJSON.put("result", "success");
        return poJSON;
    }

    public JSONObject isEntryOkay(String status) throws SQLException {
        poJSON = new JSONObject();

        GValidator loValidator = new APPaymentAdjustmentValidator();
        loValidator.setApplicationDriver(poGRider);
        loValidator.setTransactionStatus(status);
        loValidator.setMaster(poModel);
        poJSON = loValidator.validate();
        return poJSON;
    }

    @Override
    public JSONObject isEntryOkay() throws SQLException {
        poJSON = new JSONObject();
        poModel.setModifyingBy(poGRider.Encrypt(poGRider.getUserID()));
        poModel.setModifiedDate(poGRider.getServerDate());

        poJSON.put("result", "success");
        return poJSON;
    }

    @Override
    public Model_AP_Payment_Adjustment getModel() {
        return poModel;
    }

//    public JSONObject searchTransaction() throws SQLException, GuanzonException {
//        String lsSQL = getSQ_Browse();
//
//        poJSON = ShowDialogFX.Search(poGRider,
//                lsSQL,
//                "",
//                "ID»Description»Account",
//                "sPrtclrID»sDescript»xAcctDesc",
//                "a.sPrtclrID»a.sDescript»IFNULL(b.sDescript, '')",
//                1);
//
//        if (poJSON != null) {
//            return poModel.openRecord((String) poJSON.get("sPrtclrID"));
//        } else {
//            poJSON = new JSONObject();
//            poJSON.put("result", "error");
//            poJSON.put("message", "No record loaded.");
//            return poJSON;
//        }
//    }

    public JSONObject searchTransaction()
            throws CloneNotSupportedException,
            SQLException,
            GuanzonException {
        poJSON = new JSONObject();
        String lsTransStat = "";
        String lsSQL = getSQ_Browse();

        System.out.println("Executing SQL: " + lsSQL);
        poJSON = ShowDialogFX.Browse(poGRider,
                lsSQL,
                "",
                "Transaction Date»Transaction No»Payee»Company",
                "dTransact»sTransNox»sPayeeNme»sSupplrNm",
                "a.dTransact»a.sTransNox»c.sPayeeNme»b.sCompnyNm»",
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

    public JSONObject SearchClient(String value, boolean byCode)
            throws SQLException,
            GuanzonException {
        poJSON = new JSONObject();

        Client object = new ClientControllers(poGRider, logwrapr).Client();
        object.Master().setRecordStatus(RecordStatus.ACTIVE);
        object.Master().setClientType("1");
        poJSON = object.Master().searchRecord(value, byCode);
        if ("success".equals((String) poJSON.get("result"))) {
            getModel().setClientId(object.Master().getModel().getClientId());
//            getModel().setAddressId(object.ClientAddress().getModel().getAddressId()); //TODO
//            getModel().setContactId(object.ClientInstitutionContact().getModel().getClientId()); //TODO
        }

        return poJSON;
    }

    public JSONObject SearchCompany(String value, boolean byCode)
            throws SQLException,
            GuanzonException {
        poJSON = new JSONObject();

        Company object = new ParamControllers(poGRider, logwrapr).Company();
        object.setRecordStatus(RecordStatus.ACTIVE);
        poJSON = object.searchRecord(value, byCode);
        if ("success".equals((String) poJSON.get("result"))) {
            getModel().setCompanyId(object.getModel().getCompanyId());
        }
        return poJSON;
    }

    public JSONObject SearchPayee(String value, boolean byCode) throws ExceptionInInitializerError, SQLException, GuanzonException {
        Payee object = new GLControllers(poGRider, logwrapr).Payee();
        object.setRecordStatus("1");

        poJSON = object.searchRecord(value, byCode);
        if ("success".equals((String) poJSON.get("result"))) {
            getModel().setIssuedTo(object.getModel().getPayeeID());
            getModel().setPayerCode(object.getModel().getRecordStatus());
        }
        return poJSON;
    }

    @Override
    public String getSQ_Browse() {
        return " SELECT "
                + " a.dTransact "
                + " , a.sTransNox "
                + " , a.sIndstCdx "
                + " , b.sCompnyNm  AS sSupplrNm "
                + " , c.sPayeeNme  AS sPayeeNme "
                + " , d.sCompnyNm  AS sCompnyNm "
                + " , e.sDescript  AS sIndustry "
                + " FROM ap_payment_adjustment a "
                + " LEFT JOIN client_master b ON b.sClientID = a.sClientID "
                + " LEFT JOIN payee c ON c.sPayeeIDx = a.sIssuedTo "
                + " LEFT JOIN company d ON d.sCompnyID = a.sCompnyID  "
                + " LEFT JOIN industry e ON e.sIndstCdx = a.sIndstCdx ";
    }
}
