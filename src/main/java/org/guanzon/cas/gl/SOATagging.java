/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.guanzon.cas.gl;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.swing.JRViewer;
import net.sf.jasperreports.swing.JRViewerToolbar;
import net.sf.jasperreports.view.JasperViewer;
import org.guanzon.appdriver.agent.ShowDialogFX;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.agent.services.Transaction;
import org.guanzon.appdriver.agent.systables.SysTableContollers;
import org.guanzon.appdriver.agent.systables.TransactionAttachment;
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
import org.guanzon.cas.gl.model.Model_AP_Payment_Detail;
import org.guanzon.cas.gl.model.Model_AP_Payment_Master;
import org.guanzon.cas.gl.services.SOATaggingControllers;
import org.guanzon.cas.gl.services.SOATaggingModels;
import org.guanzon.cas.gl.status.SOATaggingStatus;
import org.guanzon.cas.inv.InvSerial;
import org.guanzon.cas.inv.Inventory;
import org.guanzon.cas.inv.InventoryTransaction;
import org.guanzon.cas.inv.services.InvControllers;
import org.guanzon.cas.inv.services.InvModels;
import org.guanzon.cas.parameter.Brand;
import org.guanzon.cas.parameter.Company;
import org.guanzon.cas.parameter.InvLocation;
import org.guanzon.cas.parameter.Term;
import org.guanzon.cas.parameter.services.ParamControllers;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

/**
 *
 * @author User
 */
public class SOATagging extends Transaction {

    private boolean pbApproval = false;
    private boolean pbIsPrint = false;
    private boolean pbIsWithDiscount = false;
    private boolean pbIsWithDiscountRate = false;
    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psCategorCd = "";

    List<Model_AP_Payment_Master> paMasterList;
    List<Model> paDetailRemoved;

    public JSONObject InitTransaction() {
        SOURCE_CODE = "SOA";

        poMaster = new SOATaggingModels(poGRider).SOATaggingMaster();
        poDetail = new SOATaggingModels(poGRider).SOATaggingDetails();

        paMasterList = new ArrayList<>();
        paDetail = new ArrayList<>();
//        paOthers = new ArrayList<>();
        paDetailRemoved = new ArrayList<>();
        
        return initialize();
    }

    public JSONObject NewTransaction()
            throws CloneNotSupportedException {
        return newTransaction();
    }

    public JSONObject SaveTransaction()
            throws SQLException,
            GuanzonException,
            CloneNotSupportedException {
        return saveTransaction();
    }

    public JSONObject OpenTransaction(String transactionNo)
            throws CloneNotSupportedException,
            SQLException,
            GuanzonException {
        //Clear data
        resetMaster();
        resetOthers();
        Detail().clear();
        return openTransaction(transactionNo);
    }

    public JSONObject UpdateTransaction() {
        return updateTransaction();
    }

    public JSONObject ConfirmTransaction(String remarks)
            throws ParseException,
            SQLException,
            GuanzonException,
            CloneNotSupportedException {
        poJSON = new JSONObject();

        String lsStatus = SOATaggingStatus.CONFIRMED;
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
//        poJSON = isEntryOkay(SOATaggingStatus.CONFIRMED);
//        if (!"success".equals((String) poJSON.get("result"))) {
//            return poJSON;
//        }

        //Update others
//        poJSON = setValueToOthers(lsStatus);
//        if (!"success".equals((String) poJSON.get("result"))) {
//            return poJSON;
//        }
//        if (pbApproval) {
//            if (poGRider.getUserLevel() == UserRight.ENCODER) {
//                poJSON = ShowDialogFX.getUserApproval(poGRider);
//                if (!"success".equals((String) poJSON.get("result"))) {
//                    return poJSON;
//                }
//            }
//        }

        poGRider.beginTrans("UPDATE STATUS", "ConfirmTransaction", SOURCE_CODE, Master().getTransactionNo());

        //change status
        poJSON = statusChange(poMaster.getTable(), (String) poMaster.getValue("sTransNox"), remarks, lsStatus, !lbConfirm, true);
        if (!"success".equals((String) poJSON.get("result"))) {
            poGRider.rollbackTrans();
            return poJSON;
        }

        //Update Purchase Order, Inventory, Serial Ledger
//        poJSON = saveUpdateOthers(SOATaggingStatus.CONFIRMED);
//        if (!"success".equals((String) poJSON.get("result"))) {
//            poGRider.rollbackTrans();
//            return poJSON;
//        }
//
//        //Update Inventory Serial
//        poJSON = saveUpdateInvSerial(SOATaggingStatus.CONFIRMED);
//        if (!"success".equals((String) poJSON.get("result"))) {
//            poGRider.rollbackTrans();
//            return poJSON;
//        }

        poGRider.commitTrans();

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

        String lsStatus = SOATaggingStatus.RETURNED;
        boolean lbReturn = true;

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

        //validator
        poJSON = isEntryOkay(SOATaggingStatus.RETURNED);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        if (SOATaggingStatus.CONFIRMED.equals(Master().getTransactionStatus())) {
            if (poGRider.getUserLevel() == UserRight.ENCODER) {
                poJSON = ShowDialogFX.getUserApproval(poGRider);
                if (!"success".equals((String) poJSON.get("result"))) {
                    return poJSON;
                }
            }
            
//            poJSON = setValueToOthers(lsStatus);
//            if (!"success".equals((String) poJSON.get("result"))) {
//                return poJSON;
//            }
        }

        poGRider.beginTrans("UPDATE STATUS", "ReturnTransaction", SOURCE_CODE, Master().getTransactionNo());

        //change status
        poJSON = statusChange(poMaster.getTable(), (String) poMaster.getValue("sTransNox"), remarks, lsStatus, !lbReturn, true);
        if (!"success".equals((String) poJSON.get("result"))) {
            poGRider.rollbackTrans();
            return poJSON;
        }
        
//        if (SOATaggingStatus.CONFIRMED.equals(Master().getTransactionStatus())) {
//            //Update Purchase Order, Inventory, Serial Ledger
//            poJSON = saveUpdateOthers(SOATaggingStatus.CONFIRMED);
//            if (!"success".equals((String) poJSON.get("result"))) {
//                poGRider.rollbackTrans();
//                return poJSON;
//            }
//        }

        poGRider.commitTrans();

        poJSON = new JSONObject();
        poJSON.put("result", "success");
        if (lbReturn) {
            poJSON.put("message", "Transaction returned successfully.");
        } else {
            poJSON.put("message", "Transaction return request submitted successfully.");
        }

        return poJSON;
    }
    
    public JSONObject ApproveTransaction(String remarks)
            throws ParseException,
            SQLException,
            GuanzonException,
            CloneNotSupportedException {
//        poJSON = new JSONObject();
//
//        String lsStatus = SOATaggingStatus.APPROVED;
//        boolean lbApprove = true;
//
//        if (getEditMode() != EditMode.READY) {
//            poJSON.put("result", "error");
//            poJSON.put("message", "No transacton was loaded.");
//            return poJSON;
//        }
//
//        if (lsStatus.equals((String) poMaster.getValue("cTranStat"))) {
//            poJSON.put("result", "error");
//            poJSON.put("message", "Transaction was already approved.");
//            return poJSON;
//        }
//
//        //validator
//        poJSON = isEntryOkay(SOATaggingStatus.APPROVED);
//        if (!"success".equals((String) poJSON.get("result"))) {
//            return poJSON;
//        }
//
//        //Update purchase order
//        poJSON = setValueToOthers(lsStatus);
//        if (!"success".equals((String) poJSON.get("result"))) {
//            return poJSON;
//        }
//
//        if (pbApproval) {
//            if (poGRider.getUserLevel() == UserRight.ENCODER) {
//                poJSON = ShowDialogFX.getUserApproval(poGRider);
//                if (!"success".equals((String) poJSON.get("result"))) {
//                    return poJSON;
//                }
//            }
//        }
//
//        poGRider.beginTrans("UPDATE STATUS", "ApproveTransaction", SOURCE_CODE, Master().getTransactionNo());
//
//        //change status
//        poJSON = statusChange(poMaster.getTable(), (String) poMaster.getValue("sTransNox"), remarks, lsStatus, !lbApprove, true);
//        if (!"success".equals((String) poJSON.get("result"))) {
//            poGRider.rollbackTrans();
//            return poJSON;
//        }
//
//        //Update Purchase Order, Serial Ledger, Inventory
//        poJSON = saveUpdateOthers(SOATaggingStatus.APPROVED);
//        if (!"success".equals((String) poJSON.get("result"))) {
//            poGRider.rollbackTrans();
//            return poJSON;
//        }
//
//        poGRider.commitTrans();
//
//        poJSON = new JSONObject();
//        poJSON.put("result", "success");
//        if (lbApprove) {
//            poJSON.put("message", "Transaction approved successfully.");
//        } else {
//            poJSON.put("message", "Transaction approved request submitted successfully.");
//        }
//
        return poJSON;
    }

    public JSONObject PaidTransaction(String remarks)
            throws ParseException,
            SQLException,
            GuanzonException,
            CloneNotSupportedException {
        poJSON = new JSONObject();

        String lsStatus = SOATaggingStatus.PAID;
        boolean lbPaid = true;

        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "No transacton was loaded.");
            return poJSON;
        }

        if (lsStatus.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already paid.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(SOATaggingStatus.PAID);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        //Update purchase order
//        poJSON = setValueToOthers(lsStatus);
//        if (!"success".equals((String) poJSON.get("result"))) {
//            return poJSON;
//        }
        if (pbApproval) {
            if (poGRider.getUserLevel() == UserRight.ENCODER) {
                poJSON = ShowDialogFX.getUserApproval(poGRider);
                if (!"success".equals((String) poJSON.get("result"))) {
                    return poJSON;
                }
            }
        }

        poGRider.beginTrans("UPDATE STATUS", "PaidTransaction", SOURCE_CODE, Master().getTransactionNo());

        //change status
        poJSON = statusChange(poMaster.getTable(), (String) poMaster.getValue("sTransNox"), remarks, lsStatus, !lbPaid, true);
        if (!"success".equals((String) poJSON.get("result"))) {
            poGRider.rollbackTrans();
            return poJSON;
        }

        //Update Purchase Order, Serial Ledger, Inventory
//        poJSON = saveUpdateOthers(SOATaggingStatus.PAID);
//        if (!"success".equals((String) poJSON.get("result"))) {
//            poGRider.rollbackTrans();
//            return poJSON;
//        }

        poGRider.commitTrans();

        poJSON = new JSONObject();
        poJSON.put("result", "success");
        if (lbPaid) {
            poJSON.put("message", "Transaction paid successfully.");
        } else {
            poJSON.put("message", "Transaction paid request submitted successfully.");
        }

        return poJSON;
    }

    public JSONObject ProcessTransaction(String remarks)
            throws ParseException,
            SQLException,
            GuanzonException,
            CloneNotSupportedException {
        poJSON = new JSONObject();

        String lsStatus = SOATaggingStatus.PROCESSED;
        boolean lbPosted = true;

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
        poJSON = isEntryOkay(SOATaggingStatus.PROCESSED);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        //change status
        poJSON = statusChange(poMaster.getTable(), (String) poMaster.getValue("sTransNox"), remarks, lsStatus, !lbPosted);

        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poJSON = new JSONObject();
        poJSON.put("result", "success");
        if (lbPosted) {
            poJSON.put("message", "Transaction posted successfully.");
        } else {
            poJSON.put("message", "Transaction posting request submitted successfully.");
        }

        return poJSON;
    }

    public JSONObject CancelTransaction(String remarks)
            throws ParseException,
            SQLException,
            GuanzonException,
            CloneNotSupportedException {
        poJSON = new JSONObject();

        String lsStatus = SOATaggingStatus.CANCELLED;
        boolean lbCancelled = true;

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
        poJSON = isEntryOkay(SOATaggingStatus.CANCELLED);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        if (SOATaggingStatus.CONFIRMED.equals(Master().getTransactionStatus())) {
            if (poGRider.getUserLevel() == UserRight.ENCODER) {
                poJSON = ShowDialogFX.getUserApproval(poGRider);
                if (!"success".equals((String) poJSON.get("result"))) {
                    return poJSON;
                }
            }

            //update Purchase Order
//            poJSON = setValueToOthers(lsStatus);
//            if (!"success".equals((String) poJSON.get("result"))) {
//                return poJSON;
//            }
        }

        poGRider.beginTrans("UPDATE STATUS", "CancelledTransaction", SOURCE_CODE, Master().getTransactionNo());

        //change status
        poJSON = statusChange(poMaster.getTable(), (String) poMaster.getValue("sTransNox"), remarks, lsStatus, !lbCancelled, true);
        if (!"success".equals((String) poJSON.get("result"))) {
            poGRider.rollbackTrans();
            return poJSON;
        }

//        if (SOATaggingStatus.CONFIRMED.equals(Master().getTransactionStatus())) {
//            //Update Purchase Order, Serial Ledger, Inventory
//            poJSON = saveUpdateOthers(SOATaggingStatus.CONFIRMED);
//            if (!"success".equals((String) poJSON.get("result"))) {
//                poGRider.rollbackTrans();
//                return poJSON;
//            }
//        }

        //Delete Inventory Serial
//        poJSON = deleteInvSerial();
//        if (!"success".equals((String) poJSON.get("result"))) {
//            poGRider.rollbackTrans();
//            return poJSON;
//        }

        poGRider.commitTrans();

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

        String lsStatus = SOATaggingStatus.VOID;
        boolean lbVoid = true;

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
        poJSON = isEntryOkay(SOATaggingStatus.VOID);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        if (SOATaggingStatus.CONFIRMED.equals(Master().getTransactionStatus())) {
            if (poGRider.getUserLevel() == UserRight.ENCODER) {
                poJSON = ShowDialogFX.getUserApproval(poGRider);
                if (!"success".equals((String) poJSON.get("result"))) {
                    return poJSON;
                }
            }

            //update Purchase Order
//            poJSON = setValueToOthers(lsStatus);
//            if (!"success".equals((String) poJSON.get("result"))) {
//                return poJSON;
//            }
        }

        poGRider.beginTrans("UPDATE STATUS", "VoidTransaction", SOURCE_CODE, Master().getTransactionNo());

        //change status
        poJSON = statusChange(poMaster.getTable(), (String) poMaster.getValue("sTransNox"), remarks, lsStatus, !lbVoid, true);
        if (!"success".equals((String) poJSON.get("result"))) {
            poGRider.rollbackTrans();
            return poJSON;
        }

//        if (SOATaggingStatus.CONFIRMED.equals(Master().getTransactionStatus())) {
//            //Update Purchase Order, Serial Ledger, Inventory
//            poJSON = saveUpdateOthers(SOATaggingStatus.CONFIRMED);
//            if (!"success".equals((String) poJSON.get("result"))) {
//                poGRider.rollbackTrans();
//                return poJSON;
//            }
//        }

        poGRider.commitTrans();

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

    public void setCategoryId(String categoryId) {
        psCategorCd = categoryId;
    }

    public JSONObject searchTransaction()
            throws CloneNotSupportedException,
            SQLException,
            GuanzonException {
        poJSON = new JSONObject();
        String lsTransStat = "";
        if (psTranStat != null) {
            if (psTranStat.length() > 1) {
                for (int lnCtr = 0; lnCtr <= psTranStat.length() - 1; lnCtr++) {
                    lsTransStat += ", " + SQLUtil.toSQL(Character.toString(psTranStat.charAt(lnCtr)));
                }
                lsTransStat = " AND a.cTranStat IN (" + lsTransStat.substring(2) + ")";
            } else {
                lsTransStat = " AND a.cTranStat = " + SQLUtil.toSQL(psTranStat);
            }
        }

        initSQL();
        String lsSQL = MiscUtil.addCondition(SQL_BROWSE, " a.sIndstCdx = " + SQLUtil.toSQL(psIndustryId)
                + " AND a.sCompnyID = " + SQLUtil.toSQL(psCompanyId)
                + " AND a.sCategrCd = " + SQLUtil.toSQL(psCategorCd)
                + " AND a.sBranchCD = " + SQLUtil.toSQL(poGRider.getBranchCode())
                + " AND a.sSupplier LIKE " + SQLUtil.toSQL("%" + Master().getClientId()));
        if (psTranStat != null && !"".equals(psTranStat)) {
            lsSQL = lsSQL + lsTransStat;
        }

        System.out.println("Executing SQL: " + lsSQL);
        poJSON = ShowDialogFX.Browse(poGRider,
                lsSQL,
                "",
                "Transaction Date»Transaction No»Industry»Company»Supplier",
                "dTransact»sTransNox»sIndustry»sCompnyNm»sSupplrNm",
                "a.dTransact»a.sTransNox»d.sDescript»c.sCompnyNm»b.sCompnyNm",
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

    public JSONObject searchTransaction(String industryId, String companyId, String supplier, String sReferenceNo)
            throws CloneNotSupportedException,
            SQLException,
            GuanzonException {
        if (supplier == null) {
            supplier = "";
        }
        if (sReferenceNo == null) {
            sReferenceNo = "";
        }

        if (industryId == null || "".equals(industryId)) {
            industryId = psIndustryId;
        }

        if (companyId == null || "".equals(companyId)) {
            companyId = psCompanyId;
        }

        poJSON = new JSONObject();
        String lsTransStat = "";
        if (psTranStat != null) {
            if (psTranStat.length() > 1) {
                for (int lnCtr = 0; lnCtr <= psTranStat.length() - 1; lnCtr++) {
                    lsTransStat += ", " + SQLUtil.toSQL(Character.toString(psTranStat.charAt(lnCtr)));
                }
                lsTransStat = " AND a.cTranStat IN (" + lsTransStat.substring(2) + ")";
            } else {
                lsTransStat = " AND a.cTranStat = " + SQLUtil.toSQL(psTranStat);
            }
        }

        initSQL();
        String lsSQL = MiscUtil.addCondition(SQL_BROWSE, " a.sIndstCdx = " + SQLUtil.toSQL(industryId)
                + " AND a.sCompnyID = " + SQLUtil.toSQL(companyId)
                + " AND a.sCategrCd = " + SQLUtil.toSQL(psCategorCd)
                + " AND a.sBranchCD = " + SQLUtil.toSQL(poGRider.getBranchCode())
                + " AND b.sCompnyNm LIKE " + SQLUtil.toSQL("%" + supplier)
                + " AND a.sTransNox LIKE " + SQLUtil.toSQL("%" + sReferenceNo));
        if (psTranStat != null && !"".equals(psTranStat)) {
            lsSQL = lsSQL + lsTransStat;
        }

        System.out.println("Executing SQL: " + lsSQL);
        poJSON = ShowDialogFX.Browse(poGRider,
                lsSQL,
                "",
                "Transaction Date»Transaction No»Industry»Company»Supplier",
                "dTransact»sTransNox»sIndustry»sCompnyNm»sSupplrNm",
                "a.dTransact»a.sTransNox»d.sDescript»c.sCompnyNm»b.sCompnyNm",
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

    public JSONObject searchTransaction(String industryId, String companyId, String categoryId, String supplierId, String transactionNo, String referenceNo)
            throws CloneNotSupportedException,
            SQLException,
            GuanzonException {
        boolean lbByCode = false;
        if (supplierId == null) {
            supplierId = "";
        }
        if (referenceNo == null) {
            referenceNo = "";
            lbByCode = true;
        }
        if (transactionNo == null) {
            transactionNo = "";
        }
        poJSON = new JSONObject();
        String lsTransStat = "";
        if (psTranStat != null) {
            if (psTranStat.length() > 1) {
                for (int lnCtr = 0; lnCtr <= psTranStat.length() - 1; lnCtr++) {
                    lsTransStat += ", " + SQLUtil.toSQL(Character.toString(psTranStat.charAt(lnCtr)));
                }
                lsTransStat = " AND a.cTranStat IN (" + lsTransStat.substring(2) + ")";
            } else {
                lsTransStat = " AND a.cTranStat = " + SQLUtil.toSQL(psTranStat);
            }
        }

        initSQL();
        String lsSQL = MiscUtil.addCondition(SQL_BROWSE, " a.sIndstCdx = " + SQLUtil.toSQL(industryId)
                + " AND a.sCompnyID = " + SQLUtil.toSQL(companyId)
                + " AND a.sCategrCd = " + SQLUtil.toSQL(categoryId)
                + " AND a.sBranchCD = " + SQLUtil.toSQL(poGRider.getBranchCode())
                + " AND a.sSupplier LIKE " + SQLUtil.toSQL("%" + supplierId)
                + " AND a.sTransNox LIKE " + SQLUtil.toSQL("%" + transactionNo)
                + " AND a.sReferNox LIKE " + SQLUtil.toSQL("%" + referenceNo));
        if (psTranStat != null && !"".equals(psTranStat)) {
            lsSQL = lsSQL + lsTransStat;
        }

        System.out.println("Executing SQL: " + lsSQL);
        poJSON = ShowDialogFX.Browse(poGRider,
                lsSQL,
                "",
                "Transaction Date»Transaction No»Reference No»Industry»Company»Supplier",
                "dTransact»sTransNox»sReferNox»sIndustry»sCompnyNm»sSupplrNm",
                "a.dTransact»a.sTransNox»a.sReferNox»d.sDescript»c.sCompnyNm»b.sCompnyNm",
                lbByCode ? 1 : 2);

        if (poJSON != null) {
            return Master().openRecord((String) poJSON.get("sTransNox"));
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;
        }
    }

    @Override
    public int getDetailCount() {
        if (paDetail == null) {
            paDetail = new ArrayList<>();
        }

        return paDetail.size();
    }

    public JSONObject AddDetail()
            throws CloneNotSupportedException {
        poJSON = new JSONObject();

        if (getDetailCount() > 0) {
            if (Detail(getDetailCount() - 1).getSourceCode() != null) {
                if (Detail(getDetailCount() - 1).getSourceCode().isEmpty()) {
                    poJSON.put("result", "error");
                    poJSON.put("message", "Last row has empty item.");
                    return poJSON;
                }
            }
        }

        return addDetail();
    }

    public int getDetailRemovedCount() {
        if (paDetailRemoved == null) {
            paDetailRemoved = new ArrayList<>();
        }

        return paDetailRemoved.size();
    }

    public Model_AP_Payment_Master DetailRemove(int row) {
        return (Model_AP_Payment_Master) paDetailRemoved.get(row);
    }

    /*Search Master References*/
    public JSONObject SearchCompany(String value, boolean byCode)
            throws SQLException,
            GuanzonException {
        poJSON = new JSONObject();

        Company object = new ParamControllers(poGRider, logwrapr).Company();
        object.setRecordStatus(RecordStatus.ACTIVE);
        poJSON = object.searchRecord(value, byCode);
        if ("success".equals((String) poJSON.get("result"))) {
            Master().setCompanyId(object.getModel().getCompanyId());
        }
        return poJSON;
    }

    public JSONObject SearchSupplier(String value, boolean byCode)
            throws SQLException,
            GuanzonException {
        poJSON = new JSONObject();

        Client object = new ClientControllers(poGRider, logwrapr).Client();
        object.Master().setRecordStatus(RecordStatus.ACTIVE);
        object.Master().setClientType("1");
        poJSON = object.Master().searchRecord(value, byCode);
        if ("success".equals((String) poJSON.get("result"))) {
            Master().setClientId(object.Master().getModel().getClientId());
        }

        return poJSON;
    }
    
    public JSONObject computeFields()
            throws SQLException,
            GuanzonException {
        poJSON = new JSONObject();

        //Compute Transaction Total

        return poJSON;
    }

    /*Convert Date to String*/
    private static String xsDateShort(Date fdValue) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String date = sdf.format(fdValue);
        return date;
    }

    private LocalDate strToDate(String val) {
        DateTimeFormatter date_formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate localDate = LocalDate.parse(val, date_formatter);
        return localDate;
    }

    public JSONObject computeDiscountRate(double discount) {
        poJSON = new JSONObject();
//        Double ldblTotal = 0.00;
//        Double ldblDiscRate = 0.00;
//
//        for (int lnCtr = 0; lnCtr <= getDetailCount() - 1; lnCtr++) {
//            ldblTotal += (Detail(lnCtr).getAppliedAmount().doubleValue() * Detail(lnCtr).getQuantity().intValue());
//        }
//        
//        if (discount < 0 || discount > ldblTotal) {
//            Master().setDiscount(0.00);
//            computeDiscountRate(0.00);
//            poJSON.put("result", "error");
//            poJSON.put("message", "Discount amount cannot be negative or exceed the transaction total.");
//            return poJSON;
//        } else {
////            ldblDiscRate = (discount / ldblTotal) * 100;
////            ldblDiscRate = (discount / ldblTotal);
//            //nettotal = total - discount - rate
////            Master().setDiscountRate(ldblDiscRate);
//
//            ldblTotal = ldblTotal - discount - ((Master().getDiscountRate().doubleValue() / 100.00) * ldblTotal);
//            if(ldblTotal < 0 ){
//                poJSON.put("result", "error");
//                poJSON.put("message", "Invalid transaction total.");
//                return poJSON;
//            }
//        }
        poJSON.put("result", "success");
        poJSON.put("message", "success");
        return poJSON;
    }

    public JSONObject computeDiscount(double discountRate) {
        poJSON = new JSONObject();
        Double ldblTotal = 0.00;
        Double ldblDiscount = 0.00;

//        for (int lnCtr = 0; lnCtr <= getDetailCount() - 1; lnCtr++) {
//            ldblTotal += (Detail(lnCtr).getUnitPrce().doubleValue() * Detail(lnCtr).getQuantity().intValue());
//        }
//
//        if (discountRate < 0 || discountRate > 100.00) {
////        if (discountRate < 0 || discountRate > 1.00) {
//            Master().setDiscountRate(0.00);
//            computeDiscount(0.00);
//            poJSON.put("result", "error");
//            poJSON.put("message", "Discount rate cannot be negative or exceed 100.00");
//            return poJSON;
//        } else {;
////            ldblDiscount = ldblTotal * (discountRate / 100.00);
////            ldblDiscount = ldblTotal * discountRate;
//            //nettotal = total - discount - rate
////            Master().setDiscount(ldblDiscount);
//
//            ldblTotal = ldblTotal - Master().getDiscount().doubleValue() - ((discountRate / 100.00) * ldblTotal);
//            if(ldblTotal < 0 ){
//                poJSON.put("result", "error");
//                poJSON.put("message", "Invalid transaction total.");
//                return poJSON;
//            }
//        }
//
//        poJSON.put("result", "success");
//        poJSON.put("message", "success");
        return poJSON;
    }

    public JSONObject removeSOATaggingDetails() {
        poJSON = new JSONObject();
        Iterator<Model> detail = Detail().iterator();
        while (detail.hasNext()) {
            Model item = detail.next();
            detail.remove();
        }
        
        poJSON.put("result", "success");
        poJSON.put("message", "success");
        return poJSON;
    }
    
    public JSONObject loadSOATagging(String companyId, String supplierId, String referenceNo) {
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
            if (psTranStat != null) {
                if (psTranStat.length() > 1) {
                    for (int lnCtr = 0; lnCtr <= psTranStat.length() - 1; lnCtr++) {
                        lsTransStat += ", " + SQLUtil.toSQL(Character.toString(psTranStat.charAt(lnCtr)));
                    }
                    lsTransStat = " AND a.cTranStat IN (" + lsTransStat.substring(2) + ")";
                } else {
                    lsTransStat = " AND a.cTranStat = " + SQLUtil.toSQL(psTranStat);
                }
            }
            
            initSQL();
            String lsSQL = MiscUtil.addCondition(SQL_BROWSE, //" a.sIndstCdx = " + SQLUtil.toSQL(psIndustryId)
                    " a.sCompnyID = " + SQLUtil.toSQL(companyId)
                    + " AND a.sCategrCd = " + SQLUtil.toSQL(psCategorCd)
                    + " AND a.sBranchCd = " + SQLUtil.toSQL(poGRider.getBranchCode())
                    + " AND a.sSupplier LIKE " + SQLUtil.toSQL("%" + supplierId)
                    + " AND a.sTransNox LIKE " + SQLUtil.toSQL("%" + referenceNo)
            );

            if (psIndustryId == null || "".equals(psIndustryId)) {
                lsSQL = lsSQL + " AND (a.sIndstCdx = '' OR a.sIndstCdx = null) ";
            } else {
                lsSQL = lsSQL + " AND a.sIndstCdx = " + SQLUtil.toSQL(psIndustryId);
            }

            lsSQL = lsSQL + " " + lsTransStat + " ORDER BY a.dTransact DESC ";

            System.out.println("Executing SQL: " + lsSQL);
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            poJSON = new JSONObject();

            int lnctr = 0;

            if (MiscUtil.RecordCount(loRS) >= 0) {
                paMasterList = new ArrayList<>();
                while (loRS.next()) {
                    // Print the result set
                    System.out.println("sTransNox: " + loRS.getString("sTransNox"));
                    System.out.println("dTransact: " + loRS.getDate("dTransact"));
                    System.out.println("sCompnyNm: " + loRS.getString("sCompnyNm"));
                    System.out.println("------------------------------------------------------------------------------");

                    paMasterList.add(SOATaggingMaster());
                    paMasterList.get(paMasterList.size() - 1).openRecord(loRS.getString("sTransNox"));
                    lnctr++;
                }

                System.out.println("Records found: " + lnctr);
                poJSON.put("result", "success");
                poJSON.put("message", "Record loaded successfully.");
            } else {
                paMasterList = new ArrayList<>();
                paMasterList.add(SOATaggingMaster());
                poJSON.put("result", "error");
                poJSON.put("continue", true);
                poJSON.put("message", "No record found.");
            }
            MiscUtil.close(loRS);
        } catch (SQLException e) {
            poJSON.put("result", "error");
            poJSON.put("message", e.getMessage());
        } catch (GuanzonException ex) {
            Logger.getLogger(SOATagging.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            poJSON.put("result", "error");
            poJSON.put("message", MiscUtil.getException(ex));
        }
        return poJSON;
    }

    private Model_AP_Payment_Master SOATaggingMaster() {
        return new SOATaggingModels(poGRider).SOATaggingMaster();
    }

    public Model_AP_Payment_Master PurchaseOrderReceivingList(int row) {
        return (Model_AP_Payment_Master) paMasterList.get(row);
    }

    public int getSOATaggingCount() {
        return this.paMasterList.size();
    }
    
//    private String getCategory(){
//        switch(psIndustryId){
//            case "01": //Mobile Phone
//                if("0001".equals(psCategorCd)){
//                    return SQLUtil.toSQL(psCategorCd);
//                } else {
//                    return SQLUtil.toSQL("0005") + ", " +  SQLUtil.toSQL("0006");
//                }
//            case "02": //Motorcycle
//                if("0010".equals(psCategorCd)){
//                    return SQLUtil.toSQL(psCategorCd);
//                }
//                //Spare Parts, Accessories , Giveaways
//                if("0011".equals(psCategorCd)){
//                    return SQLUtil.toSQL(psCategorCd) + ", " + SQLUtil.toSQL("0012") + ", " +  SQLUtil.toSQL("0013");
//                } else {
//                    return SQLUtil.toSQL("0017") + ", " +  SQLUtil.toSQL("0018");
//                }
//            case "03": //Vehicle
//                if("0015".equals(psCategorCd)){
//                    return SQLUtil.toSQL(psCategorCd);
//                }
//                //Spare Parts
//                if("0016".equals(psCategorCd)){
//                    return SQLUtil.toSQL(psCategorCd);
//                } else { //Accessories , Giveaways
//                    return SQLUtil.toSQL("0017") + ", " +  SQLUtil.toSQL("0018");
//                }
//            case "04": //Hospitality
//                if("0023".equals(psCategorCd)){
//                    return SQLUtil.toSQL(psCategorCd);
//                } else { // Food Service, Baked Goods TODO GENERAL
//                    return SQLUtil.toSQL("0021") + ", " +  SQLUtil.toSQL("0022");
//                }
//            case "05": //Los Pedritos
//                // Food Service, Baked Goods TODO GENERAL
//                return SQLUtil.toSQL("0019") + ", " +  SQLUtil.toSQL("0020");
//            case "06": //Main Office
//                return SQLUtil.toSQL(psCategorCd);
//        }
//        
//        return psCategorCd;
//    }
//    public JSONObject getApprovedPurchaseOrder() {
//        try {
//            String lsSupplier = Master().getSupplierId().isEmpty()
//                    ? " (a.sSupplier = null OR TRIM(a.sSupplier) = '')"
//                    : " a.sSupplier = " + SQLUtil.toSQL(Master().getSupplierId());
//            paPOMaster = new ArrayList<>();
//            String lsSQL = " SELECT "
//                    + "   a.sTransNox "
//                    + " , a.dTransact "
//                    + " , c.sCompnyNm AS sSupplier "
//                    + " FROM po_master a "
//                    + " LEFT JOIN po_detail b on b.sTransNox = a.sTransNox "
//                    + " LEFT JOIN client_master c ON c.sClientID = a.sSupplier ";
//            
//            if(poGRider.isMainOffice() || poGRider.isWarehouse()){
//                lsSQL = MiscUtil.addCondition(lsSQL, " a.sIndstCdx = " + SQLUtil.toSQL(psIndustryId)
//                        + " AND a.sCompnyID LIKE " + SQLUtil.toSQL("%" + psCompanyId)
//                        + " AND a.sSupplier LIKE " + SQLUtil.toSQL("%"+ Master().getSupplierId())
//                        + " AND a.cTranStat = " + SQLUtil.toSQL(PurchaseOrderStatus.APPROVED)
//                        + " AND a.sCategrCd = "+ SQLUtil.toSQL(psCategorCd)
//                        + " AND b.nQuantity > b.nReceived "
//                );
//            } else {
//                lsSQL = MiscUtil.addCondition(lsSQL, " a.sIndstCdx = " + SQLUtil.toSQL(psIndustryId)
//                        + " AND a.sCompnyID LIKE " + SQLUtil.toSQL("%" + psCompanyId)
//                        + " AND a.sSupplier LIKE " + SQLUtil.toSQL("%"+ Master().getSupplierId())
//                        + " AND a.sDestinat = " + SQLUtil.toSQL(poGRider.getBranchCode())
//                        + " AND a.cTranStat = " + SQLUtil.toSQL(PurchaseOrderStatus.APPROVED)
//                        + " AND a.sCategrCd = "+ SQLUtil.toSQL(psCategorCd)
//                        + " AND b.nQuantity > b.nReceived "
//                );
//            }
//  
//            lsSQL = lsSQL + " GROUP BY a.sTransNox "
//                    + " ORDER BY dTransact ASC";
//            
//            System.out.println("Executing SQL: " + lsSQL);
//
//            ResultSet loRS = poGRider.executeQuery(lsSQL);
//            poJSON = new JSONObject();
//            int lnctr = 0;
//
//            if (MiscUtil.RecordCount(loRS) > 0) {
//                while (loRS.next()) {
//                    // Print the result set
//                    System.out.println("sTransNox: " + loRS.getString("sTransNox"));
//                    System.out.println("dTransact: " + loRS.getDate("dTransact"));
//                    System.out.println("sSupplier: " + loRS.getString("sSupplier"));
//                    System.out.println("------------------------------------------------------------------------------");
//
//                    paPOMaster.add(CachePayables());
//                    paPOMaster.get(paPOMaster.size() - 1).openRecord(loRS.getString("sTransNox"));
//                    lnctr++;
//                }
//
//                System.out.println("Records found: " + lnctr);
//                poJSON.put("result", "success");
//                poJSON.put("message", "Record loaded successfully.");
//            } else {
//                poJSON.put("result", "error");
//                poJSON.put("continue", true);
//                poJSON.put("message", "No approved purchase order found .");
//            }
//            MiscUtil.close(loRS);
//        } catch (SQLException ex) {
//            poJSON.put("result", "error");
//            poJSON.put("continue", false);
//            poJSON.put("message", ex.getMessage());
//        }
//        return poJSON;
//    }
//    private Model_Cache_Payables_Master CachePayableMaster() {
//        return new CachePayablesModels(poGRider).CachePayables();
//    }
//
//    public Model_PO_Master CachePayableList(int row) {
//        return (Model_PO_Master) paPOMaster.get(row);
//    }
//    public int getCachePayablesCount() {
//        if (paPOMaster == null) {
//            paPOMaster = new ArrayList<>();
//        }
//
//        return paPOMaster.size();
//    }
    public JSONObject addCachePayablesToSOATaggingDetail(String transactionNo)
            throws CloneNotSupportedException,
            SQLException,
            GuanzonException {
        poJSON = new JSONObject();
//        boolean lbExist = false;
//        boolean lbReceived = false;
//        int lnRow = 0;
//        int lnAddOrderQty = 0;
//        CachePayablesControllers loTrans = new CachePayablesControllers(poGRider, logwrapr);
//        poJSON = loTrans.PurchaseOrder().InitTransaction();
//        if ("success".equals((String) poJSON.get("result"))) {
//            poJSON = loTrans.PurchaseOrder().OpenTransaction(transactionNo);
//            if ("success".equals((String) poJSON.get("result"))) {
//                for (int lnCtr = 0; lnCtr <= loTrans.PurchaseOrder().getDetailCount() - 1; lnCtr++) {
//                    
//                    //Check existing supplier
//                    if(Master().getSupplierId() == null || "".equals(Master().getSupplierId())){
//                        Master().setSupplierId(loTrans.PurchaseOrder().Master().getSupplierID());
//                    } else {
//                        if(!Master().getSupplierId().equals(loTrans.PurchaseOrder().Master().getSupplierID())){
//                            if(getDetailCount() >= 0 ){
//                                poJSON.put("result", "error");
//                                poJSON.put("message", "Supplier must be equal to selected purchase order supplier.");
//                                return poJSON;
//                            } else {
//                                Master().setSupplierId(loTrans.PurchaseOrder().Master().getSupplierID());
//                            }
//                        }
//                    }
//                    
//                    for (lnRow = 0; lnRow <= getDetailCount() - 1; lnRow++) {
//                        if (Detail(lnRow).getOrderNo() != null && !"".equals(Detail(lnRow).getOrderNo())) {
//                            //check when pre-owned po is already exist in detail. 
//                            //if exist only pre-owned purchase order will allow to insert in por detail 
//                            if (Detail(lnRow).PurchaseOrderMaster().getPreOwned() != loTrans.PurchaseOrder().Master().getPreOwned()) {
//                                poJSON.put("result", "error");
//                                poJSON.put("message", "Purchase orders for pre-owned items cannot be combined with purchase orders for new items.");
//                                return poJSON;
//                            }
//                        }
//                        
//                        if (Detail(lnRow).getOrderNo().equals(loTrans.PurchaseOrder().Detail(lnCtr).getTransactionNo())
//                                && (Detail(lnRow).getStockId().equals(loTrans.PurchaseOrder().Detail(lnCtr).getStockID()))) {
//                            lbExist = true;
//                            break;
//                        }
//                    }
//
//                    if (!lbExist) {
//                        //Only insert po detail that has item to receive
//                        if (loTrans.PurchaseOrder().Detail(lnCtr).getQuantity().intValue() > loTrans.PurchaseOrder().Detail(lnCtr).getReceivedQuantity().intValue()) {
//                            Detail(getDetailCount() - 1).setBrandId(loTrans.PurchaseOrder().Detail(lnCtr).Inventory().getBrandId());
//                            Detail(getDetailCount() - 1).setOrderNo(loTrans.PurchaseOrder().Detail(lnCtr).getTransactionNo());
//                            Detail(getDetailCount() - 1).setStockId(loTrans.PurchaseOrder().Detail(lnCtr).getStockID());
//                            Detail(getDetailCount() - 1).setUnitType(loTrans.PurchaseOrder().Detail(lnCtr).Inventory().getUnitType());
//                            Detail(getDetailCount() - 1).setOrderQty(loTrans.PurchaseOrder().Detail(lnCtr).getQuantity().intValue() - loTrans.PurchaseOrder().Detail(lnCtr).getReceivedQuantity().intValue());
//                            Detail(getDetailCount() - 1).setWhCount(loTrans.PurchaseOrder().Detail(lnCtr).getQuantity().intValue() - loTrans.PurchaseOrder().Detail(lnCtr).getReceivedQuantity().intValue());
//                            Detail(getDetailCount() - 1).setUnitPrce(loTrans.PurchaseOrder().Detail(lnCtr).getUnitPrice());
//                            Detail(getDetailCount() - 1).isSerialized(loTrans.PurchaseOrder().Detail(lnCtr).Inventory().isSerialized());
//
//                            AddDetail();
//                            lbReceived = true;
//                        }
//                    } else {
//                        //sum order qty based on existing stock id in POR Detail
//                        for (int lnOrder = 0; lnOrder <= loTrans.PurchaseOrder().getDetailCount() - 1; lnOrder++) {
//                            if(Detail(lnRow).getOrderNo().equals(loTrans.PurchaseOrder().Detail(lnOrder).getTransactionNo())){
//                                if(Detail(lnRow).getStockId().equals(loTrans.PurchaseOrder().Detail(lnOrder).getStockID())){
//                                    lnAddOrderQty = lnAddOrderQty + (loTrans.PurchaseOrder().Detail(lnOrder).getQuantity().intValue() - loTrans.PurchaseOrder().Detail(lnOrder).getReceivedQuantity().intValue());
//                                }
//                            }
//                        }
//                        
//                        Detail(lnRow).setOrderQty(lnAddOrderQty);
//                        lbReceived = true;
//                    }
//                    
//                    lbExist = false;
//                    lnAddOrderQty = 0;
//                }
//                
//                if(!lbReceived){
//                    poJSON.put("result", "error");
//                    poJSON.put("message", "No remaining order to be receive for Order No. " + transactionNo + ".");
//                    return poJSON;
//                }
//            } else {
//                poJSON.put("result", "error");
//                poJSON.put("message", "No records found.");
//            }
//        } else {
//            poJSON.put("result", "error");
//            poJSON.put("message", "No records found.");
//        }
        return poJSON;
    }
    public JSONObject addPaymentRequestToSOATaggingDetail(String transactionNo)
            throws CloneNotSupportedException,
            SQLException,
            GuanzonException {
        poJSON = new JSONObject();
        boolean lbExist = false;
        boolean lbReceived = false;
        int lnRow = 0;
        int lnAddOrderQty = 0;
        PaymentRequest loTrans = new PaymentRequest();
        poJSON = loTrans.InitTransaction();
        if ("success".equals((String) poJSON.get("result"))) {
            poJSON = loTrans.OpenTransaction(transactionNo);
            if ("success".equals((String) poJSON.get("result"))) {
                for (int lnCtr = 0; lnCtr <= loTrans.getDetailCount() - 1; lnCtr++) {
                    
                    //Check existing supplier
                    if(Master().getClientId() == null || "".equals(Master().getClientId())){
                        Master().setClientId(loTrans.Master().getPayeeID());
                    } else {
                        if(!Master().getClientId().equals(loTrans.Master().getPayeeID())){
                            if(getDetailCount() >= 0 ){
                                poJSON.put("result", "error");
                                poJSON.put("message", "Supplier must be equal to selected purchase order supplier.");
                                return poJSON;
                            } else {
                                Master().setClientId(loTrans.Master().getPayeeID());
                            }
                        }
                    }
                    
                    for (lnRow = 0; lnRow <= getDetailCount() - 1; lnRow++) {
                        if (Detail(lnRow).getSourceNo()!= null && !"".equals(Detail(lnRow).getSourceNo())) {
                            //check when pre-owned po is already exist in detail. 
                            //if exist only pre-owned purchase order will allow to insert in por detail 
//                            if (Detail(lnRow).PurchaseOrderMaster().getPreOwned() != loTrans.PurchaseOrder().Master().getPreOwned()) {
//                                poJSON.put("result", "error");
//                                poJSON.put("message", "Purchase orders for pre-owned items cannot be combined with purchase orders for new items.");
//                                return poJSON;
//                            }
                        }
                        
                        if (Detail(lnRow).getSourceNo().equals(loTrans.Detail(lnCtr).getTransactionNo())
                                && (Detail(lnRow).getSourceCode().equals(loTrans.Detail(lnCtr).getEntryNo()))) {
                            lbExist = true;
                            break;
                        }
                    }

//                    if (!lbExist) {
//                        //Only insert po detail that has item to receive
////                        if (loTrans.Detail(lnCtr).getQuantity().intValue() > loTrans.PurchaseOrder().Detail(lnCtr).getReceivedQuantity().intValue()) {
//                            Detail(getDetailCount() - 1).setBrandId(loTrans.Detail(lnCtr).Inventory().getBrandId());
//                            Detail(getDetailCount() - 1).setOrderNo(loTrans.Detail(lnCtr).getTransactionNo());
//                            Detail(getDetailCount() - 1).setStockId(loTrans.Detail(lnCtr).getStockID());
//                            Detail(getDetailCount() - 1).setUnitType(loTrans.Detail(lnCtr).Inventory().getUnitType());
//                            Detail(getDetailCount() - 1).setOrderQty(loTrans.Detail(lnCtr).getQuantity().intValue() - loTrans.Detail(lnCtr).getReceivedQuantity().intValue());
//                            Detail(getDetailCount() - 1).setWhCount(loTrans.Detail(lnCtr).getQuantity().intValue() - loTrans.Detail(lnCtr).getReceivedQuantity().intValue());
//                            Detail(getDetailCount() - 1).setUnitPrce(loTrans.Detail(lnCtr).getUnitPrice());
//                            Detail(getDetailCount() - 1).isSerialized(loTrans.Detail(lnCtr).Inventory().isSerialized());
//
//                            AddDetail();
//                            lbReceived = true;
////                        }
//                    } else {
//                        //sum order qty based on existing stock id in POR Detail
//                        for (int lnOrder = 0; lnOrder <= loTrans.PurchaseOrder().getDetailCount() - 1; lnOrder++) {
//                            if(Detail(lnRow).getOrderNo().equals(loTrans.PurchaseOrder().Detail(lnOrder).getTransactionNo())){
//                                if(Detail(lnRow).getStockId().equals(loTrans.PurchaseOrder().Detail(lnOrder).getStockID())){
//                                    lnAddOrderQty = lnAddOrderQty + (loTrans.PurchaseOrder().Detail(lnOrder).getQuantity().intValue() - loTrans.PurchaseOrder().Detail(lnOrder).getReceivedQuantity().intValue());
//                                }
//                            }
//                        }
//                        
//                        Detail(lnRow).setOrderQty(lnAddOrderQty);
//                        lbReceived = true;
//                    }
                    
                    lbExist = false;
                    lnAddOrderQty = 0;
                }
                
                if(!lbReceived){
                    poJSON.put("result", "error");
                    poJSON.put("message", "No remaining order to be receive for Order No. " + transactionNo + ".");
                    return poJSON;
                }
            } else {
                poJSON.put("result", "error");
                poJSON.put("message", "No records found.");
            }
        } else {
            poJSON.put("result", "error");
            poJSON.put("message", "No records found.");
        }
        return poJSON;
    }
    
    public void resetOthers() {
//        paOthers = new ArrayList<>();
    }

    public void resetMaster() {
        poMaster = new SOATaggingModels(poGRider).SOATaggingMaster();
    }
    
    @Override
    public String getSourceCode() {
        return SOURCE_CODE;
    }

    @Override
    public Model_AP_Payment_Master Master() {
        return (Model_AP_Payment_Master) poMaster;
    }

    @Override
    public Model_AP_Payment_Detail Detail(int row) {
        return (Model_AP_Payment_Detail) paDetail.get(row);
    }

    public Model_AP_Payment_Master getDetail() {
        return (Model_AP_Payment_Master) poDetail;
    }

    @Override
    public JSONObject willSave()
            throws SQLException,
            GuanzonException,
            CloneNotSupportedException {
        /*Put system validations and other assignments here*/
        poJSON = new JSONObject();
        boolean lbUpdated = false;
        
        if (paDetailRemoved == null) {
            paDetailRemoved = new ArrayList<>();
        }
        
        if(!pbIsPrint){
            if (!xsDateShort(poGRider.getServerDate()).equals(xsDateShort(Master().getTransactionDate()))  && getEditMode() == EditMode.ADDNEW ){
                if (poGRider.getUserLevel() == UserRight.ENCODER) {
                    poJSON = ShowDialogFX.getUserApproval(poGRider);
                    if (!"success".equals((String) poJSON.get("result"))) {
                        return poJSON;
                    }
                }
            }
        }

        Master().setModifyingId(poGRider.getUserID());
        Master().setModifiedDate(poGRider.getServerDate());
        
        boolean lbHasQty = false;
        int lnEntryNo = 0;
        int lnNewEntryNo = 1;
        int lnPrevEntryNo = -1;
        boolean lbMatch = false;
        
        Iterator<Model> detail = Detail().iterator();
        while (detail.hasNext()) {
            Model item = detail.next();

//            if ("".equals((String) item.getValue("sStockIDx"))
//                    || (int) item.getValue("nQuantity") <= 0) {
//                detail.remove();
//
//                if (!"".equals((String) item.getValue("sOrderNox")) && (String) item.getValue("sOrderNox") != null) {
//                    paDetailRemoved.add(item);
//                }
//            }
        }
        
        //Validate detail after removing all zero qty and empty stock Id
        if (getDetailCount() <= 0) {
            poJSON.put("result", "error");
            poJSON.put("message", "No SOA detail to be save.");
            return poJSON;
        }

        if (getDetailCount() == 1) {
            //do not allow a single item detail with no quantity order
//            if (Detail(0).getQuantity().intValue() == 0) {
//                poJSON.put("result", "error");
//                poJSON.put("message", "Your Purchase order receiving has zero quantity.");
//                return poJSON;
//            }
        }

        if (getEditMode() == EditMode.UPDATE) {
            SOATagging loRecord = new SOATaggingControllers(poGRider, null).SOATagging();
            loRecord.InitTransaction();
            loRecord.OpenTransaction(Master().getTransactionNo());

            //Set original supplier Id
            if(!Master().getClientId().equals(loRecord.Master().getClientId())){
                Master().setClientId(loRecord.Master().getClientId());
            }
            
            if(!pbIsPrint){
                if (SOATaggingStatus.CONFIRMED.equals(Master().getTransactionStatus())
                        || !xsDateShort(loRecord.Master().getTransactionDate()).equals(xsDateShort(Master().getTransactionDate()))) {
                    if (poGRider.getUserLevel() == UserRight.ENCODER) {
                        poJSON = ShowDialogFX.getUserApproval(poGRider);
                        if (!"success".equals((String) poJSON.get("result"))) {
                            return poJSON;
                        }
                    }
                }

                if (SOATaggingStatus.RETURNED.equals(Master().getTransactionStatus())) {

                    lbUpdated = loRecord.getDetailCount() == getDetailCount();
                    if (lbUpdated) {
                        lbUpdated = loRecord.Master().getTransactionTotal().doubleValue() == Master().getTransactionTotal().doubleValue();
                    }
                    if (lbUpdated) {
                        lbUpdated = loRecord.Master().getRemarks().equals(Master().getRemarks());
                    }

                    if (lbUpdated) {
                        for (int lnCtr = 0; lnCtr <= loRecord.getDetailCount() - 1; lnCtr++) {
//                            lbUpdated = loRecord.Detail(lnCtr).getStockId().equals(Detail(lnCtr).getStockId());
//                            if (lbUpdated) {
//                                lbUpdated = loRecord.Detail(lnCtr).getQuantity().equals(Detail(lnCtr).getQuantity());
//                            } 
//
//                            if (!lbUpdated) {
//                                break;
//                            }
//

                        }
                    }

                    if (lbUpdated) {
                        poJSON.put("result", "error");
                        poJSON.put("message", "No update has been made.");
                        return poJSON;
                    }

//                    Master().setPrint("0"); 
                    Master().setTransactionStatus(SOATaggingStatus.OPEN); //If edited update trasaction status into open
                
                }
            }
        }

        //assign other info on detail
        for (int lnCtr = 0; lnCtr <= getDetailCount() - 1; lnCtr++) {
            //Set value to por detail
            Detail(lnCtr).setTransactionNo(Master().getTransactionNo());
            Detail(lnCtr).setEntryNo(lnCtr + 1);
            Detail(lnCtr).setModifiedDate(poGRider.getServerDate());
        }

        //Allow the user to edit details but seek an approval from the approving officer
//        if (SOATaggingStatus.CONFIRMED.equals(Master().getTransactionStatus())) {
//            poJSON = setValueToOthers(Master().getTransactionStatus());
//            if (!"success".equals((String) poJSON.get("result"))) {
//                return poJSON;
//            }
//        }

        poJSON.put("result", "success");
        return poJSON;
    }
    @Override
    public JSONObject save() {
//        try {
//            /*Put saving business rules here*/
            return isEntryOkay(SOATaggingStatus.OPEN);
//        } catch (CloneNotSupportedException ex) {
//            Logger.getLogger(SOATagging.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (SQLException ex) {
//            Logger.getLogger(SOATagging.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (GuanzonException ex) {
//            Logger.getLogger(SOATagging.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return null;
    }

//    @Override
//    public JSONObject saveOthers() {
//        /*Only modify this if there are other tables to modify except the master and detail tables*/
//        poJSON = new JSONObject();
//        int lnCtr, lnRow;
//
//        try {
//            //Purchase Order Receiving Serial
//            InvSerial loInvSerial = new InvControllers(poGRider, logwrapr).InventorySerial();
//            loInvSerial.setWithParentClass(true);
//
//            for (lnRow = 0; lnRow <= getPurchaseOrderReceivingSerialCount() - 1; lnRow++) {
//                //1. Check for Serial ID
//                if ("".equals(paOthers.get(lnRow).getSerialId()) || paOthers.get(lnRow).getSerialId() == null) {
//                    //1.1 Create New Inventory Serial
//                    poJSON = loInvSerial.newRecord();
//                    if ("error".equals((String) poJSON.get("result"))) {
//                        System.out.println("inv serial " + (String) poJSON.get("message"));
//                        return poJSON;
//                    }
//                } else {
//                    //1.2 Update Inventory Serial / Registration
//                    poJSON = loInvSerial.openRecord(paOthers.get(lnRow).getSerialId());
//                    if ("error".equals((String) poJSON.get("result"))) {
//                        return poJSON;
//                    }
//                    System.out.println(loInvSerial.getEditMode());
//                    poJSON = loInvSerial.updateRecord();
//                    if ("error".equals((String) poJSON.get("result"))) {
//                        return poJSON;
//                    }
//                }
//                
//                //2. Update values for serial
//                if (loInvSerial.getEditMode() == EditMode.ADDNEW || loInvSerial.getEditMode() == EditMode.UPDATE) {
//                    loInvSerial.getModel().setStockId(paOthers.get(lnRow).getStockId());
//                    loInvSerial.getModel().setSerial01(paOthers.get(lnRow).getSerial01());
//                    loInvSerial.getModel().setSerial02(paOthers.get(lnRow).getSerial02());
//                    loInvSerial.getModel().setUnitType(paOthers.get(lnRow).Inventory().getUnitType());
//                    
////                    if(poGRider.isWarehouse()){
////                        loInvSerial.getModel().setLocation("0"); 
////                    } else {
////                        loInvSerial.getModel().setLocation("1"); 
////                    }
//                    
//                    //Only set location of inv serial into 1 when confirmed according to ma'am she 05152025
//                    if (SOATaggingStatus.CONFIRMED.equals(Master().getTransactionStatus())) {
//                        loInvSerial.getModel().setLocation("1"); 
//                    } else {
//                        loInvSerial.getModel().setLocation("0"); 
//                    }
//
//                    //2.1 Only set branch code and company id during creation of serial in por
//                    if (loInvSerial.getEditMode() == EditMode.ADDNEW) {
//                        loInvSerial.getModel().setBranchCode(poGRider.getBranchCode());
//                        loInvSerial.getModel().setCompnyId(Master().getCompanyId());
//                    }
//                    
//                    if (!"".equals(paOthers.get(lnRow).getPlateNo()) && paOthers.get(lnRow).getPlateNo() != null) {
//                        loInvSerial.SerialRegistration().setPlateNoP(paOthers.get(lnRow).getPlateNo());
//                    }
//                    
//                    if (!"".equals(paOthers.get(lnRow).getConductionStickerNo()) && paOthers.get(lnRow).getConductionStickerNo() != null){
//                        loInvSerial.SerialRegistration().setConductionStickerNo(paOthers.get(lnRow).getConductionStickerNo());
//                    }
//
//                    //3. Validation Serial
//                    poJSON = loInvSerial.isEntryOkay();
//                    if ("error".equals((String) poJSON.get("result"))) {
//                        System.out.println("inv serial validation : " + (String) poJSON.get("message"));
//                        return poJSON;
//                    }
//
//                    //4. Save Inventory Serial
//                    System.out.println("----------------------SAVE INV SERIAL---------------------- ");
//                    System.out.println("Serial ID  : " + loInvSerial.getModel().getSerialId());
//                    System.out.println("Serial 01  : " + loInvSerial.getModel().getSerial01());
//                    System.out.println("Serial 02  : " + loInvSerial.getModel().getSerial02());
//                    System.out.println("Location   : " + loInvSerial.getModel().getLocation());
//                    System.out.println("Edit Mode  : " + loInvSerial.getEditMode());
//                    System.out.println("---------------------------------------------------------------------- ");
//                    poJSON = loInvSerial.saveRecord();
//                    if ("error".equals((String) poJSON.get("result"))) {
//                        System.out.println("inv serial saving" + (String) poJSON.get("message"));
//                        return poJSON;
//                    }
//                }
//                //5. Set serial id to por serial
//                if (paOthers.get(lnRow).getSerialId().equals("") || paOthers.get(lnRow).getSerialId() == null) {
//                    paOthers.get(lnRow).setSerialId(loInvSerial.getModel().getSerialId());
//                }
//                //6. Save Purchase Order Receiving Serial
//                System.out.println("----------------------SAVE PURCHASE ORDER RECEIVING SERIAL---------------------- ");
//                System.out.println("Transaction No  : " + paOthers.get(lnRow).getTransactionNo());
//                System.out.println("Entry No  : " + paOthers.get(lnRow).getEntryNo());
//                System.out.println("Serial ID : " + paOthers.get(lnRow).getSerialId());
//                System.out.println("Location  : " + paOthers.get(lnRow).getLocationId());
//                System.out.println("Edit Mode : " + paOthers.get(lnRow).getEditMode());
//                System.out.println("---------------------------------------------------------------------- ");
//                paOthers.get(lnRow).setTransactionNo(Master().getTransactionNo());
//                paOthers.get(lnRow).setModifiedDate(poGRider.getServerDate());
//                poJSON = paOthers.get(lnRow).saveRecord();
//                if ("error".equals((String) poJSON.get("result"))) {
//                    return poJSON;
//                }
//            }
//
//            //Save Attachments
//            for (lnCtr = 0; lnCtr <= getTransactionAttachmentCount() - 1; lnCtr++) {
//                if (paAttachments.get(lnCtr).getEditMode() == EditMode.ADDNEW || paAttachments.get(lnCtr).getEditMode() == EditMode.UPDATE) {
//                    paAttachments.get(lnCtr).getModel().setModifyingId(poGRider.getUserID());
//                    paAttachments.get(lnCtr).getModel().setModifiedDate(poGRider.getServerDate());
//                    paAttachments.get(lnCtr).setWithParentClass(true);
//                    poJSON = paAttachments.get(lnCtr).saveRecord();
//                    if ("error".equals((String) poJSON.get("result"))) {
//                        return poJSON;
//                    }
//                }
//            }
//
//            //Save Purchase Order, Serial Ledger, Inventory
//            if (SOATaggingStatus.CONFIRMED.equals(Master().getTransactionStatus())) {
//                poJSON = saveUpdateOthers(SOATaggingStatus.CONFIRMED);
//                if (!"success".equals((String) poJSON.get("result"))) {
//                    return poJSON;
//                }
//            }
//
//        } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
//            Logger.getLogger(SOATagging.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
//            poJSON.put("result", "error");
//            poJSON.put("message", MiscUtil.getException(ex));
//            return poJSON;
//        }
//        poJSON.put("result", "success");
//        return poJSON;
//    }
//    private PurchaseOrder PurchaseOrder() {
//        return new PurchaseOrderControllers(poGRider, logwrapr).PurchaseOrder();
//    }
//    private InventoryTransaction InventoryTransaction(){
//        return new InventoryTransactionControllers(poGRider, logwrapr).InventoryTransaction();
//    }
//    private JSONObject setValueToOthers(String status)
//            throws CloneNotSupportedException,
//            SQLException,
//            GuanzonException {
//        poJSON = new JSONObject();
//        paPurchaseOrder = new ArrayList<>();
//        paInventoryTransaction = new ArrayList<>();
//        int lnCtr;
//
//        //Update Purchase Order exist in PO Receiving Detail
//        for (lnCtr = 0; lnCtr <= getDetailCount() - 1; lnCtr++) {
//            System.out.println("----------------------PURCHASE ORDER RECEIVING DETAIL---------------------- ");
//            System.out.println("TransNo : " + (lnCtr + 1) + " : " + Detail(lnCtr).getTransactionNo());
//            System.out.println("OrderNo : " + (lnCtr + 1) + " : " + Detail(lnCtr).getOrderNo());
//            System.out.println("StockId : " + (lnCtr + 1) + " : " + Detail(lnCtr).getStockId());
//            System.out.println("------------------------------------------------------------------ ");
//            if (Detail(lnCtr).getOrderNo() != null && !"".equals(Detail(lnCtr).getOrderNo())) {
//                //1. Check for discrepancy
//                if (Detail(lnCtr).getOrderQty().intValue() != Detail(lnCtr).getQuantity().intValue()) {
//                    System.out.println("Require Approval");
//                    pbApproval = true;
//                }
//                //Purchase Order
//                poJSON = updatePurchaseOrder(status, Detail(lnCtr).getOrderNo(), Detail(lnCtr).getStockId(), Detail(lnCtr).getQuantity().intValue());
//                if("error".equals((String) poJSON.get("result"))){
//                    return poJSON;
//                }
//
//                //Inventory Transaction
//                if(Detail(lnCtr).getReplaceId() != null && !"".equals(Detail(lnCtr).getReplaceId())){
//                    updateInventoryTransaction(status, Detail(lnCtr).getReplaceId(), Detail(lnCtr).getQuantity().intValue());
//                } else {
//                    updateInventoryTransaction(status, Detail(lnCtr).getStockId(), Detail(lnCtr).getQuantity().intValue());
//                }
//
//            } else {
//                //Require approve for all po receiving without po
//                System.out.println("Require Approval");
//                pbApproval = true;
//            }
//        }
//
//        //Update purchase order removed in purchase order receiving
//        for (lnCtr = 0; lnCtr <= getDetailRemovedCount() - 1; lnCtr++) {
//            //Purchase Order
//            poJSON = updatePurchaseOrder(status, DetailRemove(lnCtr).getOrderNo(), DetailRemove(lnCtr).getStockId(), DetailRemove(lnCtr).getQuantity().intValue());
//            if("error".equals((String) poJSON.get("result"))){
//                return poJSON;
//            }
//            
//            //Inventory Transaction TODO
//            if(DetailRemove(lnCtr).getReplaceId() != null && !"".equals(DetailRemove(lnCtr).getReplaceId())){
//                updateInventoryTransaction(status, DetailRemove(lnCtr).getReplaceId(), DetailRemove(lnCtr).getQuantity().intValue());
//            } else {
//                updateInventoryTransaction(status, DetailRemove(lnCtr).getStockId(), DetailRemove(lnCtr).getQuantity().intValue());
//            }
//        }
//
//        poJSON.put("result", "success");
//        return poJSON;
//    }
//    private JSONObject updatePurchaseOrder(String status, String orderNo, String stockId, int quantity)
//            throws GuanzonException,
//            SQLException,
//            CloneNotSupportedException {
//        int lnRow, lnList;
//        int lnRecQty = 0;
//        int lnOrderQty = 0;
//        boolean lbExist = false;
//        //2.check if order no is already exist in purchase order array list
//        for (lnRow = 0; lnRow <= paPurchaseOrder.size() - 1; lnRow++) {
//            System.out.println("paPurchaseOrder.get(lnRow).Master().getTransactionNo() : " + paPurchaseOrder.get(lnRow).Master().getTransactionNo());
//            if (paPurchaseOrder.get(lnRow).Master().getTransactionNo() != null) {
//                if (orderNo.equals(paPurchaseOrder.get(lnRow).Master().getTransactionNo())) {
//                    lbExist = true;
//                    break;
//                }
//            }
//        }
//
//        //3. If order no is not exist add it on puchase order array list then open the transaction
//        if (!lbExist) {
//            paPurchaseOrder.add(PurchaseOrder());
//            paPurchaseOrder.get(paPurchaseOrder.size() - 1).InitTransaction();
//            paPurchaseOrder.get(paPurchaseOrder.size() - 1).OpenTransaction(orderNo);
//            paPurchaseOrder.get(paPurchaseOrder.size() - 1).UpdateTransaction();
//            lnList = paPurchaseOrder.size() - 1;
//        } else {
//            //if already exist, get the row no of purchase order
//            lnList = lnRow;
//        }
//        
//        switch (status) {
//            case SOATaggingStatus.CONFIRMED:
//            case SOATaggingStatus.PAID:
//            case SOATaggingStatus.POSTED:
//                //Get total received qty from other po receiving entry
//                lnRecQty = getReceivedQty(orderNo, stockId, true);
//                //Add received qty in po receiving
//                lnRecQty = lnRecQty + quantity;
//                
//                for (lnRow = 0; lnRow <= paPurchaseOrder.get(lnList).getDetailCount() - 1; lnRow++) {
//                    if (stockId.equals(paPurchaseOrder.get(lnList).Detail(lnRow).getStockID())) {
//                        lnOrderQty = lnOrderQty + paPurchaseOrder.get(lnList).Detail(lnRow).getQuantity().intValue();
//                    }
//                }
//                
//                if(lnRecQty > lnOrderQty){
//                    poJSON.put("result", "error");
//                    poJSON.put("message", "Confirmed receive quantity cannot be greater than the order quantity for Order No. " + orderNo);
//                    return poJSON;
//                }
//                
//                break;
//            case SOATaggingStatus.VOID:
//            case SOATaggingStatus.RETURNED:
//                //Get total received qty from other po receiving entry
//                lnRecQty = getReceivedQty(orderNo, stockId, false);
//                //Deduct received qty in po receiving
//                lnRecQty = lnRecQty - quantity;
//                break;
//        }
//        
//        for (lnRow = 0; lnRow <= paPurchaseOrder.get(lnList).getDetailCount() - 1; lnRow++) {
//            if (stockId.equals(paPurchaseOrder.get(lnList).Detail(lnRow).getStockID())) {
//                //set Receive qty in Purchase Order detail
//                if(lnRecQty <= 0){
//                    lnRecQty = 0;
//                    paPurchaseOrder.get(lnList).Detail(lnRow).setReceivedQuantity(0);
//                } else {
//                    if(lnRecQty > paPurchaseOrder.get(lnList).Detail(lnRow).getQuantity().intValue()){
//                        paPurchaseOrder.get(lnList).Detail(lnRow).setReceivedQuantity(paPurchaseOrder.get(lnList).Detail(lnRow).getQuantity());
//                        lnRecQty = lnRecQty - paPurchaseOrder.get(lnList).Detail(lnRow).getQuantity().intValue();
//                    } else {
//                        paPurchaseOrder.get(lnList).Detail(lnRow).setReceivedQuantity(lnRecQty);
//                        lnRecQty = 0;
//                    }
//                }
//
//                paPurchaseOrder.get(lnList).Detail(lnRow).setModifiedDate(poGRider.getServerDate());
//            }
//        }
//        
//        poJSON.put("result", "success");
//        return poJSON;
//    }
    
    private int getPaidAmount(String orderNo, String stockId, boolean isAdd)
            throws SQLException,
            GuanzonException {
        poJSON = new JSONObject();
        int lnRecQty = 0;
        String lsSQL = " SELECT "
                + " b.nQuantity AS nQuantity "
                + " FROM po_receiving_master a "
                + " LEFT JOIN po_receiving_detail b ON b.sTransNox = a.sTransNox ";
        lsSQL = MiscUtil.addCondition(lsSQL, " b.sStockIDx = " + SQLUtil.toSQL(stockId)
                + " AND ( a.cTranStat = " + SQLUtil.toSQL(SOATaggingStatus.CONFIRMED)
                + " OR a.cTranStat = " + SQLUtil.toSQL(SOATaggingStatus.PAID)
//                + " OR a.cTranStat = " + SQLUtil.toSQL(SOATaggingStatus.POSTED)
                + " ) ");

        if (orderNo != null && !"".equals(orderNo)) {
            lsSQL = lsSQL + " AND b.sOrderNox = " + SQLUtil.toSQL(orderNo);
        }

        if (isAdd) {
            lsSQL = lsSQL + " AND a.sTransNox <> " + SQLUtil.toSQL(Master().getTransactionNo());
        }
        System.out.println("Executing SQL: " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        try {
            if (MiscUtil.RecordCount(loRS) >= 0) {
                while (loRS.next()) {
                    lnRecQty = lnRecQty + loRS.getInt("nQuantity");
                }
            }
            MiscUtil.close(loRS);
        } catch (SQLException e) {
            System.out.println("No record loaded.");
            lnRecQty = 0;
        }
        return lnRecQty;
    }

//    private JSONObject saveUpdateOthers(String status)
//            throws CloneNotSupportedException {
//        /*Only modify this if there are other tables to modify except the master and detail tables*/
//        poJSON = new JSONObject();
//        int lnCtr, lnRow;
//        try {
//
//            //1. Save Purchase Order exist in PO Receiving Detail 
//            for (lnCtr = 0; lnCtr <= paPurchaseOrder.size() - 1; lnCtr++) {
//                if(SOATaggingStatus.CONFIRMED.equals(status)){
//                    paPurchaseOrder.get(lnCtr).Master().setProcessed(true);
//                }
//                paPurchaseOrder.get(lnCtr).Master().setModifyingId(poGRider.getUserID());
//                paPurchaseOrder.get(lnCtr).Master().setModifiedDate(poGRider.getServerDate());
//                paPurchaseOrder.get(lnCtr).setWithParent(true);
//                poJSON = paPurchaseOrder.get(lnCtr).SaveTransaction();
//                if ("error".equals((String) poJSON.get("result"))) {
//                    System.out.println("Purchase Order Saving " + (String) poJSON.get("message"));
//                    return poJSON;
//                }
//            }
//
//            //2. Save Inventory Transaction TODO
//            for (lnCtr = 0; lnCtr <= paInventoryTransaction.size() - 1; lnCtr++) {
////                paInventoryTransaction.get(lnCtr).Master().setModifiedDate(poGRider.getServerDate());
////                paInventoryTransaction.get(lnCtr).setWithParent(true);
////                poJSON = paInventoryTransaction.get(lnCtr).SaveTransaction();
//                if ("error".equals((String) poJSON.get("result"))) {
//                    System.out.println("Purchase Order Saving " + (String) poJSON.get("message"));
//                    return poJSON;
//                }
//            }
//
//            //3. Save Inventory Serial Ledger TODO
//            if (SOATaggingStatus.CONFIRMED.equals(Master().getTransactionStatus())
//                    || SOATaggingStatus.PAID.equals(Master().getTransactionStatus())
//                    || SOATaggingStatus.POSTED.equals(Master().getTransactionStatus())) {
//                //Save Inventory Serial Ledger
//                //InventoryTrans.POReceiving();
//                
//            }
//        } catch (SQLException | GuanzonException ex) {
//            Logger.getLogger(SOATagging.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
//            poJSON.put("result", "error");
//            poJSON.put("message", MiscUtil.getException(ex));
//            return poJSON;
//        }
//        poJSON.put("result", "success");
//        return poJSON;
//    }

    @Override
    public void saveComplete() {
        /*This procedure was called when saving was complete*/
        System.out.println("Transaction saved successfully.");
    }

    @Override
    public JSONObject initFields() {
        try {
            /*Put initial model values here*/
            poJSON = new JSONObject();
            System.out.println("Dept ID : " + poGRider.getDepartment());
            Master().setBranchCode(poGRider.getBranchCode());
//            Master().setIndustryId(psIndustryId);
            Master().setCompanyId(psCompanyId);
//            Master().setCategoryCode(psCategorCd);
//            Master().setDepartmentId(poGRider.getDepartment());
            Master().setTransactionDate(poGRider.getServerDate());
//            Master().setReferenceDate(poGRider.getServerDate());
//            Master().setInventoryTypeCode(getInventoryTypeCode());
//            Master().setTermCode("0000004");
            Master().setTransactionStatus(SOATaggingStatus.OPEN);

        } catch (SQLException ex) {
            Logger.getLogger(SOATagging.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            poJSON.put("result", "error");
            poJSON.put("message", MiscUtil.getException(ex));
            return poJSON;
        }

        poJSON.put("result", "success");
        return poJSON;
    }

    public String getCompanyId() {
        String lsCompanyId = "";
        try {
            String lsSQL = "SELECT sCompnyID FROM branch ";
            lsSQL = MiscUtil.addCondition(lsSQL, " sBranchCd = " + SQLUtil.toSQL(poGRider.getBranchCode()));

            ResultSet loRS = poGRider.executeQuery(lsSQL);

            if (loRS.next()) {
                lsCompanyId = loRS.getString("sCompnyID");
            }

            MiscUtil.close(loRS);
        } catch (SQLException ex) {
            Logger.getLogger(SOATagging.class.getName()).log(Level.SEVERE, null, ex);
        }

        return lsCompanyId;
    }

    @Override
    public void initSQL() {
        SQL_BROWSE = " SELECT "
                + "   a.dTransact  "
                + " , a.sTransNox  "
                + " , a.sIndstCdx  "
                + " , a.sCompnyID  "
                + " , a.sSupplier  "
                + " , a.sReferNox  "
                + " , a.sCategrCd  "
                + " , b.sCompnyNm  AS sSupplrNm"
                + " , c.sCompnyNm  AS sCompnyNm"
                + " , d.sDescript  AS sIndustry"
                + " FROM ap_payment_master a "
                + " LEFT JOIN client_master b ON b.sClientID = a.sSupplier "
                + " LEFT JOIN company c ON c.sCompnyID = a.sCompnyID "
                + " LEFT JOIN industry d ON d.sIndstCdx = a.sIndstCdx ";
    }

    @Override
    protected JSONObject isEntryOkay(String status) {
//        GValidator loValidator = SOATaggingValidatorFactory.make(Master().getIndustryId());
//
//        loValidator.setApplicationDriver(poGRider);
//        loValidator.setTransactionStatus(status);
//        loValidator.setMaster(poMaster);
////        loValidator.setDetail(paDetail);
//
//        poJSON = loValidator.validate();

        return poJSON;
    }
    private CustomJasperViewer poViewer = null;
    private String psTransactionNo = "";

//    public JSONObject printRecord(Runnable onPrintedCallback) {
//        poJSON = new JSONObject();
//        String watermarkPath = "D:\\GGC_Maven_Systems\\Reports\\images\\draft.png"; //set draft as default
//        psTransactionNo = Master().getTransactionNo();
//        try {
//            
//            //Reopen Transaction to get the accurate data
//            poJSON = OpenTransaction(psTransactionNo);
//            if ("error".equals((String) poJSON.get("result"))) {
//                System.out.println("Print Record open transaction : " + (String) poJSON.get("message"));
//                return poJSON;
//            }
//            
//            // 1. Prepare parameters
//            Map<String, Object> parameters = new HashMap<>();
//            parameters.put("sSupplierNm", Master().Supplier().getCompanyName());
//            parameters.put("sBranchNm", poGRider.getBranchName()); //TODO
//            parameters.put("sAddressx", poGRider.getAddress());
//            parameters.put("sCompnyNm", poGRider.getClientName());
//            parameters.put("sTransNox", Master().getTransactionNo());
//            parameters.put("dReferDte", Master().getReferenceDate());
//            parameters.put("sReferNox", Master().getReferenceNo());
//            parameters.put("sRemarks", Master().getRemarks());
//            parameters.put("dTransDte", new java.sql.Date(Master().getTransactionDate().getTime()));
//            parameters.put("dDatexxx", new java.sql.Date(poGRider.getServerDate().getTime()));
//
//            // Set watermark based on approval status
//            switch (Master().getTransactionStatus()) {
//                case SOATaggingStatus.CONFIRMED:
//                case SOATaggingStatus.PAID:
//                case SOATaggingStatus.POSTED:
//                    if("1".equals(Master().getPrint())){
//                        watermarkPath = "D:\\GGC_Maven_Systems\\Reports\\images\\approvedreprint.png";
//                    } else {
//                        watermarkPath = "D:\\GGC_Maven_Systems\\Reports\\images\\approved.png";
//                    }
//                    break;
////                case SOATaggingStatus.CANCELLED:
////                    watermarkPath = "D:\\GGC_Maven_Systems\\Reports\\images\\cancelled.png";
////                    break;
//            }
//
//            parameters.put("watermarkImagePath", watermarkPath);
//            List<OrderDetail> orderDetails = new ArrayList<>();
//            
//            String jrxmlPath = "D:\\GGC_Maven_Systems\\Reports\\PurchaseOrderReceiving.jrxml";
//            double lnTotal = 0.0;
//            int lnRow = 1;
//            String lsDescription = "";
//            String lsSerial = "";
//            String lsBarcode = "";
//            String lsMeasure = "";
//            for (int lnCtr = 0; lnCtr <= getDetailCount() - 1; lnCtr++) {
//                lnTotal = Detail(lnCtr).getUnitPrce().doubleValue() * Detail(lnCtr).getQuantity().intValue();
//                
//                if(Detail(lnCtr).isSerialized()){
//                    getPurchaseOrderReceivingSerial(Detail(lnCtr).getEntryNo());
//                    for(int lnList = 0; lnList <=getPurchaseOrderReceivingSerialCount()-1; lnList++){
//                        if(PurchaseOrderReceivingSerialList(lnList).getEntryNo() == Detail(lnCtr).getEntryNo()){
//                            if("".equals(lsSerial)){
//                                lsSerial = PurchaseOrderReceivingSerialList(lnList).getSerial01();
//                            } else {
//                                lsSerial = lsSerial + "\n" + PurchaseOrderReceivingSerialList(lnList).getSerial01();
//                            }
//                        }
//                    }
//                }
//                
//                switch(Master().getCategoryCode()){
//                    case "0005": //CAR
//                    case "0003": //Motorcycle
//                    case "0001": //Cellphone   
//                    case "0002": //Appliances  
//                        lsBarcode = Detail(lnCtr).Inventory().Brand().getDescription();
//
//                        if(Detail(lnCtr).Inventory().Model().getDescription() != null && !"".equals(Detail(lnCtr).Inventory().Model().getDescription())){
//                            lsDescription = Detail(lnCtr).Inventory().Model().getDescription();
//                        }
//                        if(Detail(lnCtr).Inventory().Variant().getDescription() != null && !"".equals(Detail(lnCtr).Inventory().Variant().getDescription())){
//                            lsDescription = lsDescription + " " + Detail(lnCtr).Inventory().Variant().getDescription();
//                        }
//                        if(Detail(lnCtr).Inventory().Variant().getYearModel()!= 0){
//                            lsDescription = lsDescription + " " + Detail(lnCtr).Inventory().Variant().getYearModel();
//                        }
//                        if(Detail(lnCtr).Inventory().Color().getDescription() != null && !"".equals(Detail(lnCtr).Inventory().Color().getDescription())){
//                            lsDescription = lsDescription + " " + Detail(lnCtr).Inventory().Color().getDescription();
//                        }
//                        
//                        if(!"".equals(lsSerial)){
//                            lsDescription = lsDescription + "\n" + lsSerial;
//                        }
//                        orderDetails.add(new OrderDetail(lnRow, String.valueOf(Detail(lnCtr).getOrderNo()), 
//                                lsBarcode, lsDescription, Detail(lnCtr).getUnitPrce().doubleValue(), Detail(lnCtr).getQuantity().intValue(), lnTotal));
//                    break;
//                    case "0008": // Food  
//                        lsBarcode = Detail(lnCtr).Inventory().getBarCode();
//                        if (Detail(lnCtr).Inventory().Measure().getDescription() != null && !"".equals(Detail(lnCtr).Inventory().Measure().getDescription())){
//                            lsMeasure = Detail(lnCtr).Inventory().Measure().getDescription();
//                        }
//                        lsDescription = Detail(lnCtr).Inventory().Brand().getDescription() 
//                                + " " + Detail(lnCtr).Inventory().getDescription(); 
//                        orderDetails.add(new OrderDetail(lnRow, String.valueOf(Detail(lnCtr).getOrderNo()), 
//                                lsBarcode, lsDescription, lsMeasure ,Detail(lnCtr).getUnitPrce().doubleValue(), Detail(lnCtr).getQuantity().intValue(), lnTotal));
//                        jrxmlPath = "D:\\GGC_Maven_Systems\\Reports\\PurchaseOrderReceiving_Food.jrxml";
//                    break;
//                    case "0006": // CAR SP
//                    case "0004": // Motorcycle SP
//                    case "0007": // General
//                    case "0009": // Hospitality
//                    default:
//                        lsBarcode = Detail(lnCtr).Inventory().getBarCode();
//                        lsDescription = Detail(lnCtr).Inventory().getDescription();   
//                        orderDetails.add(new OrderDetail(lnRow, String.valueOf(Detail(lnCtr).getOrderNo()), 
//                                lsBarcode, lsDescription, Detail(lnCtr).getUnitPrce().doubleValue(), Detail(lnCtr).getQuantity().intValue(), lnTotal));
//                    break;
//                }
//                
//                lnRow++;
//                lsDescription = "";
//                lsBarcode = "";
//                lsSerial = "";
//            }
//
//            // 3. Create data source
//            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(orderDetails);
//
//            // 4. Compile and fill report
//            JasperReport jasperReport = JasperCompileManager.compileReport(jrxmlPath);
//            JasperPrint jasperPrint = JasperFillManager.fillReport(
//                    jasperReport,
//                    parameters,
//                    dataSource
//            );
//
//            if (poViewer != null && poViewer.isDisplayable()) {
//                poViewer.dispose();
//                poViewer = null;
//
//            }
//            poViewer = new CustomJasperViewer(jasperPrint, onPrintedCallback);
//            poViewer.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE); 
//            poViewer.setVisible(true);
//            poViewer.toFront();
//            
//            poViewer.addWindowListener(new WindowAdapter() {
//                @Override
//                public void windowClosing(java.awt.event.WindowEvent e) {
//                    poViewer = null;
//                    System.out.println("Jasper viewer is closing...");
//                }
//
//                @Override
//                public void windowClosed(java.awt.event.WindowEvent e) {
//                    System.out.println("Jasper viewer closed.");
//                    onPrintedCallback.run(); 
//                }
//            });
//            
//        } catch (JRException e) {
//            System.err.println("Error generating report: " + e.getMessage());
//            e.printStackTrace();
//            poJSON.put("result", "error");
//            poJSON.put("message", MiscUtil.getException(e));
//        } catch (SQLException | GuanzonException ex) {
//            Logger.getLogger(SOATagging.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
//            poJSON.put("result", "error");
//            poJSON.put("message", MiscUtil.getException(ex));
//        } catch (CloneNotSupportedException ex) {
//            Logger.getLogger(SOATagging.class.getName()).log(Level.SEVERE, null, ex);
//        }
//
//        return poJSON;
//    }
    public static class OrderDetail {

        private Integer nRowNo;
        private String sOrderNo;
        private String sBarcode;
        private String sDescription;
        private String sMeasure;
        private double nUprice;
        private Integer nOrder;
        private double nTotal;

        public OrderDetail(Integer rowNo, String orderNo, String barcode, String description,
                double uprice, Integer order, double total) {
            this.nRowNo = rowNo;
            this.sOrderNo = orderNo;
            this.sBarcode = barcode;
            this.sDescription = description;
            this.nUprice = uprice;
            this.nOrder = order;
            this.nTotal = total;
        }

        public OrderDetail(Integer rowNo, String orderNo, String barcode, String description, String measure,
                double uprice, Integer order, double total) {
            this.nRowNo = rowNo;
            this.sOrderNo = orderNo;
            this.sBarcode = barcode;
            this.sDescription = description;
            this.sMeasure = measure;
            this.nUprice = uprice;
            this.nOrder = order;
            this.nTotal = total;
        }

        public Integer getnRowNo() {
            return nRowNo;
        }

        public String getsOrderNo() {
            return sOrderNo;
        }

        public String getsBarcode() {
            return sBarcode;
        }

        public String getsDescription() {
            return sDescription;
        }

        public String getsMeasure() {
            return sMeasure;
        }

        public double getnUprice() {
            return nUprice;
        }

        public Integer getnOrder() {
            return nOrder;
        }

        public double getnTotal() {
            return nTotal;
        }
    }

    private class CustomJasperViewer extends JasperViewer {

        private final Runnable onPrintedCallback;

        public CustomJasperViewer(JasperPrint jasperPrint, Runnable onPrintedCallback) {
            super(jasperPrint, false);
            this.onPrintedCallback = onPrintedCallback;
            customizePrintButton(jasperPrint);

        }

        private void customizePrintButton(JasperPrint jasperPrint) {
            poJSON = new JSONObject();
            try {
                JRViewer viewer = findJRViewer(this);
                if (viewer == null) {
                    System.out.println("JRViewer not found!");
                    return;
                }

                for (int i = 0; i < viewer.getComponentCount(); i++) {
                    if (viewer.getComponent(i) instanceof JRViewerToolbar) {
                        JRViewerToolbar toolbar = (JRViewerToolbar) viewer.getComponent(i);

                        for (int j = 0; j < toolbar.getComponentCount(); j++) {
                            if (toolbar.getComponent(j) instanceof JButton) {
                                JButton button = (JButton) toolbar.getComponent(j);

                                if (button.getToolTipText() != null) {
                                    if (button.getToolTipText().equals("Save")) {
                                        button.setEnabled(false);  // Disable instead of hiding
                                        button.setVisible(false);  // Hide it completely
                                    }
                                }

                                if ("Print".equals(button.getToolTipText())) {
                                    for (ActionListener al : button.getActionListeners()) {
                                        button.removeActionListener(al);
                                    }
                                    button.addActionListener(e -> {
                                        try {
                                            boolean isPrinted = JasperPrintManager.printReport(jasperPrint, true);
                                            if (isPrinted) {
                                                PrintTransaction(true);
                                            } else {
                                                Platform.runLater(() -> {
                                                    ShowMessageFX.Warning(null, "Computerized Accounting System", "Printing was canceled by the user.");
                                                    SwingUtilities.invokeLater(() -> CustomJasperViewer.this.toFront());

                                                });
                                            }
                                        } catch (JRException ex) {
                                            Platform.runLater(() -> {
                                                ShowMessageFX.Warning(null, "Computerized Accounting System", "Print Failed: " + ex.getMessage());
                                                SwingUtilities.invokeLater(() -> CustomJasperViewer.this.toFront());
                                            });
                                        } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                                            Logger.getLogger(SOATagging.class.getName()).log(Level.SEVERE, null, ex);
                                        }
                                    });
                                }
                            }
                        }
                        // Force UI refresh after hiding the button
                        toolbar.revalidate();
                        toolbar.repaint();
                    }
                }
            } catch (Exception e) {
                System.out.println("Error customizing print button: " + e.getMessage());
            }
        }

        private void PrintTransaction(boolean fbIsPrinted)
                throws SQLException,
                CloneNotSupportedException,
                GuanzonException {
            poJSON = new JSONObject();
            if (fbIsPrinted) {
//                poJSON = OpenTransaction((String) poMaster.getValue("sTransNox"));
                poJSON = OpenTransaction(psTransactionNo);
                if ("error".equals((String) poJSON.get("result"))) {
                    Platform.runLater(() -> {
                        ShowMessageFX.Warning(null, "Print Purchase Order Receiving", "Printing of the transaction was aborted.\n" + (String) poJSON.get("message"));
                        SwingUtilities.invokeLater(() -> CustomJasperViewer.this.toFront());
                    });
                    fbIsPrinted = false;
                }
                if (SOATaggingStatus.CONFIRMED.equals(Master().getTransactionStatus())) {
                    poJSON = UpdateTransaction();
                    if ("error".equals((String) poJSON.get("result"))) {
                        Platform.runLater(() -> {
                            ShowMessageFX.Warning(null, "Print Purchase Order Receiving", "Printing of the transaction was aborted.\n" + (String) poJSON.get("message"));
                            SwingUtilities.invokeLater(() -> CustomJasperViewer.this.toFront());
                        });
                        fbIsPrinted = false;
                    }
                    //Populate purchase receiving serials
                    for (int lnCtr = 0; lnCtr <= getDetailCount() - 1; lnCtr++) {
//                        getPurchaseOrderReceivingSerial(Detail(lnCtr).getEntryNo());
                    }
                    poMaster.setValue("dModified", poGRider.getServerDate());
                    poMaster.setValue("sModified", poGRider.getUserID());
                    poMaster.setValue("cPrintxxx", Logical.YES);
                    pbIsPrint = fbIsPrinted;
                    poJSON = SaveTransaction();
                    if ("error".equals((String) poJSON.get("result"))) {
                        Platform.runLater(() -> {
                            ShowMessageFX.Warning(null, "Print Purchase Order Receiving", "Printing of the transaction was aborted.\n" + (String) poJSON.get("message"));
                            SwingUtilities.invokeLater(() -> CustomJasperViewer.this.toFront());
                        });
                        fbIsPrinted = false;
                    }

                    pbIsPrint = false;
                }
            }

            if (fbIsPrinted) {
                Platform.runLater(() -> {
                    ShowMessageFX.Information(null, "Print Purchase Order Receiving", "Transaction printed successfully.");
                });
            }

            if (onPrintedCallback != null) {
                poViewer = null;
                this.dispose();
//                onPrintedCallback.run();  // <- triggers controller method!
            }
            SwingUtilities.invokeLater(() -> CustomJasperViewer.this.toFront());
        }

        private JRViewer findJRViewer(Component parent) {
            if (parent instanceof JRViewer) {
                return (JRViewer) parent;
            }
            if (parent instanceof Container) {
                Component[] components = ((Container) parent).getComponents();
                for (Component component : components) {
                    JRViewer viewer = findJRViewer(component);
                    if (viewer != null) {
                        return viewer;
                    }
                }
            }
            return null;
        }
    }
}
