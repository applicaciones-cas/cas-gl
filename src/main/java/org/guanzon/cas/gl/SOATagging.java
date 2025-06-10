/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.guanzon.cas.gl;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;
import net.sf.jasperreports.swing.JRViewer;
import net.sf.jasperreports.swing.JRViewerToolbar;
import net.sf.jasperreports.view.JasperViewer;
import org.guanzon.appdriver.agent.ShowDialogFX;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.agent.services.Transaction;
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
import org.guanzon.cas.gl.model.Model_Payment_Request_Master;
import org.guanzon.cas.gl.services.GLControllers;
import org.guanzon.cas.gl.services.GLModels;
import org.guanzon.cas.gl.services.SOATaggingControllers;
import org.guanzon.cas.gl.services.SOATaggingModels;
import org.guanzon.cas.gl.status.PaymentRequestStatus;
import org.guanzon.cas.gl.status.SOATaggingStatic;
import org.guanzon.cas.gl.status.SOATaggingStatus;
import org.guanzon.cas.gl.validator.APPaymentValidator;
import org.guanzon.cas.parameter.Company;
import org.guanzon.cas.parameter.services.ParamControllers;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

/**
 *
 * @author Aldrich & Arsiela Team 2 05232025
 */
public class SOATagging extends Transaction {

    private boolean pbApproval = false;
    private boolean pbIsPrint = false;
    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psCategorCd = "";

    List<Model_AP_Payment_Master> paMasterList;
    List<PaymentRequest> paPaymentRequest;
    List<PaymentRequest> paCachePayable; //TODO
    List<Model> paPayablesList;
    List<String> paPayablesType;
    List<Model> paDetailRemoved;

    public JSONObject InitTransaction() {
        SOURCE_CODE = "SOA";

        poMaster = new SOATaggingModels(poGRider).SOATaggingMaster();
        poDetail = new SOATaggingModels(poGRider).SOATaggingDetails();
        
        paMasterList = new ArrayList<>();
        paDetail = new ArrayList<>();
        paDetailRemoved = new ArrayList<>();
        paPaymentRequest = new ArrayList<>();
        paCachePayable = new ArrayList<>();
        paPayablesList = new ArrayList<>();
        paPayablesType = new ArrayList<>();
        
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
        String lsSQL = MiscUtil.addCondition(SQL_BROWSE, " a.sIndstCdx = " + SQLUtil.toSQL(psIndustryId));
        if (lsTransStat != null && !"".equals(lsTransStat)) {
            lsSQL = lsSQL + lsTransStat;
        }

        System.out.println("Executing SQL: " + lsSQL);
        poJSON = ShowDialogFX.Browse(poGRider,
                lsSQL,
                "%",
                "Transaction Date»Transaction No»SOA No»Supplier",
                "dTransact»sTransNox»sSOANoxxx»sSupplrNm",
                "a.dTransact»a.sTransNox»a.sSOANoxxx»c.sCompnyNm»b.sCompnyNm",
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

    public JSONObject searchTransaction(String industryId, String company, String supplier, String sReferenceNo)
            throws CloneNotSupportedException,
            SQLException,
            GuanzonException {
        if (supplier == null) {
            supplier = "";
        }
        if (sReferenceNo == null) {
            sReferenceNo = "";
        }
        
        if (company == null || "".equals(company)) {
            company = "";
        }

        if (industryId == null || "".equals(industryId)) {
            industryId = psIndustryId;
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
                + " AND c.sCompnyNm LIKE " + SQLUtil.toSQL("%" + company)
                + " AND b.sCompnyNm LIKE " + SQLUtil.toSQL("%" + supplier)
                + " AND a.sTransNox LIKE " + SQLUtil.toSQL("%" + sReferenceNo));
        if (lsTransStat != null && !"".equals(lsTransStat)) {
            lsSQL = lsSQL + lsTransStat;
        }

        System.out.println("Executing SQL: " + lsSQL);
        poJSON = ShowDialogFX.Browse(poGRider,
                lsSQL,
                "%",
                "Transaction Date»Transaction No»SOA No»Supplier»Company",
                "dTransact»sTransNox»sSOANoxxx»sSupplrNm»sCompnyNm",
                "a.dTransact»a.sTransNox»a.sSOANoxxx»b.sCompnyNm»c.sCompnyNm",
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

    public Model_AP_Payment_Detail DetailRemove(int row) {
        return (Model_AP_Payment_Detail) paDetailRemoved.get(row);
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

    public JSONObject SearchPayee(String value, boolean byCode)
            throws SQLException,
            GuanzonException {
        poJSON = new JSONObject();

        Payee object = new GLControllers(poGRider, logwrapr).Payee();
        object.setRecordStatus(RecordStatus.ACTIVE);
        poJSON = object.searchRecord(value, byCode);
        if ("success".equals((String) poJSON.get("result"))) {
            Master().setIssuedTo(object.getModel().getPayeeID());
        }

        return poJSON;
    }
    
    public JSONObject computeFields()
            throws SQLException,
            GuanzonException {
        poJSON = new JSONObject();
        double ldblTransactionTotal = 0.0000;
        double ldblDiscountAmount = Master().getDiscountAmount().doubleValue();
        double ldblNetTotal = 0.0000;
        double ldblFreight = 0.0000;
        double ldblVatAmt = 0.0000;
        double ldblVatExemptAmt = 0.0000;
        double ldblZeroRtedAmt = 0.0000;

        //Compute Transaction Total
        for(int lnCtr = 0; lnCtr <= getDetailCount() - 1; lnCtr++){
            switch(Detail(lnCtr).getPayableType()){
                case SOATaggingStatic.PaymentRequest:
                    break;
                case SOATaggingStatic.CachePayable:
//                    ldblFreight = ldblFreight + Detail(lnCtr).CachePayable().getFreightAmt().doubleValue();
//                    ldblVatAmt = ldblVatAmt + Detail(lnCtr).CachePayable().getVatAmount().doubleValue();
//                    ldblVatExemptAmt = ldblVatExemptAmt + Detail(lnCtr).CachePayable().getVatExempt().doubleValue();
//                    ldblZeroRtedAmt = ldblZeroRtedAmt + Detail(lnCtr).CachePayable().getZeroRatedVat().doubleValue();
                    break;
            }
            
            ldblTransactionTotal = ldblTransactionTotal + Detail(lnCtr).getAppliedAmount().doubleValue();
        }
        
        Master().setTransactionTotal(ldblTransactionTotal);
        ldblNetTotal = ldblTransactionTotal - ldblDiscountAmount;
        Master().setNetTotal(ldblNetTotal);
        Master().setFreightAmount(ldblFreight);
        Master().setVatAmount(ldblVatAmt);
        Master().setVatExempt(ldblVatExemptAmt);
        Master().setZeroRatedVat(ldblZeroRtedAmt);
        
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
    
    public JSONObject loadSOATagging(String industryId, String company, String supplier, String referenceNo) {
        try {
            if (industryId == null) {
                industryId = psIndustryId;
            }
            if (company == null) {
                company = "";
            }
            if (supplier == null) {
                supplier = "";
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
            String lsSQL = MiscUtil.addCondition(SQL_BROWSE, " a.sIndstCdx = " + SQLUtil.toSQL(industryId)
                    + " AND c.sCompnyNm LIKE " + SQLUtil.toSQL("%" + company)
                    + " AND b.sCompnyNm LIKE " + SQLUtil.toSQL("%" + supplier)
                    + " AND a.sTransNox LIKE " + SQLUtil.toSQL("%" + referenceNo)
            );
            
            if (lsTransStat != null && !"".equals(lsTransStat)) {
                lsSQL = lsSQL + lsTransStat;
            }

            lsSQL = lsSQL + " ORDER BY a.dTransact DESC ";

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
                    System.out.println("sCompnyNm: " + loRS.getString("sSupplrNm"));
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

    public Model_AP_Payment_Master APPaymentMasterList(int row) {
        return (Model_AP_Payment_Master) paMasterList.get(row);
    }

    public int getSOATaggingCount() {
        if (paMasterList == null) {
            paMasterList = new ArrayList<>();
        }

        return paMasterList.size();
    }
    
    public String PayableType(int row) {
        return (String) paPayablesType.get(row);
    }
    
    private Model_Payment_Request_Master PaymentRequestMaster() {
        return new GLModels(poGRider).PaymentRequestMaster();
    }
    
    public Model_Payment_Request_Master PaymentRequestList(int row) {
        return (Model_Payment_Request_Master) paPayablesList.get(row);
    }
    
//    private Model_Cache_Payables_Master CachePayableMaster() {
//        return new CachePayablesModels(poGRider).CachePayables();
//    }
    
//    public Model_Cache_Payables_Master CachePayableList(int row) {
//        return (Model_Cache_Payables_Master) paPayablesList.get(row);
//    }
    
    public int getPayablesCount() {
        if (paPayablesList == null) {
            paPayablesList = new ArrayList<>();
        }
        if (paPayablesType == null) {
            paPayablesType = new ArrayList<>();
        }

        return paPayablesList.size();
    }
    
    public JSONObject loadPayables() {
        try {
            paPayablesList = new ArrayList<>();
            paPayablesType = new ArrayList<>();
            String lsSQL = getPayableSQL() + " ORDER BY dTransact DESC ";
            System.out.println("Executing SQL: " + lsSQL);
            System.out.println("Payment Request List");
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            poJSON = new JSONObject();
            int lnctr = 0;
            if (MiscUtil.RecordCount(loRS) >= 0) {
                while (loRS.next()) {
                    // Print the result set
                    System.out.println("sTransNox: " + loRS.getString("sTransNox"));
                    System.out.println("dTransact: " + loRS.getDate("dTransact"));
                    System.out.println("sPayablNm: " + loRS.getString("sPayablNm"));
                    System.out.println("sPayablTp: " + loRS.getString("sPayablTp"));
                    System.out.println("------------------------------------------------------------------------------");
                    
                    switch(loRS.getString("sPayablTp")){
                        case SOATaggingStatic.PaymentRequest:
                            paPayablesList.add(PaymentRequestMaster());
                            paPayablesList.get(paPayablesList.size() - 1).openRecord(loRS.getString("sTransNox"));
                            paPayablesType.add(SOATaggingStatic.PaymentRequest);
                            break;
                        case SOATaggingStatic.CachePayable:
//                            paPayablesList.add(CachePayable());
//                            paPayablesList.get(paPayablesList.size() - 1).openRecord(loRS.getString("sTransNox"));
//                            paPayablesType.add(SOATaggingStatic.CachePayable);
                            break;
                    }
                    lnctr++;
                }

                System.out.println("Records found: " + lnctr);
                poJSON.put("result", "success");
                poJSON.put("message", "Record loaded successfully.");
            } else {
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
    
    public JSONObject addPayablesToSOADetail(String transactionNo, String payableType)
            throws CloneNotSupportedException,
            SQLException,
            GuanzonException {
        poJSON = new JSONObject();
        int lnCtr = 0;
        
        //Check if transaction already exist in the list
        for(lnCtr = 0; lnCtr <= getDetailCount() - 1; lnCtr++){
            if(transactionNo.equals(Detail(lnCtr).getTransactionNo())){
                poJSON.put("result", "error");
                poJSON.put("message", "Selected transaction no "+transactionNo+" already exiting in SOA detail.");
                return poJSON;
            }
        }
        
        switch(payableType){
            case SOATaggingStatic.PaymentRequest:
                PaymentRequest loPaymentRequest = new GLControllers(poGRider, logwrapr).PaymentRequest();
                loPaymentRequest.setWithParent(true);
                loPaymentRequest.InitTransaction();
                poJSON = loPaymentRequest.OpenTransaction(transactionNo);
                if("error".equals((String) poJSON.get("result"))){
                    return poJSON;
                }
                
                Detail(getDetailCount()-1).setSourceNo(loPaymentRequest.Master().getTransactionNo());
                Detail(getDetailCount()-1).setSourceCode(loPaymentRequest.Master().getSourceCode());
                Detail(getDetailCount()-1).setTransactionTotal(loPaymentRequest.Master().getTranTotal());
                Detail(getDetailCount()-1).setDebitAmount(loPaymentRequest.Master().getTranTotal());
                Detail(getDetailCount()-1).setPayableType(payableType);
                AddDetail();
                
                break;
            case SOATaggingStatic.CachePayable:
//                CachePayable loPaymentRequest = new GLControllers(poGRider, logwrapr).CachePayable();
//                loCachePayable.InitTransaction();
//                poJSON = loCachePayable.OpenTransaction(transactionNo);
//                if("error".equals((String) poJSON.get("result"))){
//                    return poJSON;
//                }
//                
//                Detail(getDetailCount()-1).setSourceNo(loCachePayable.Master().getTransactionNo());
//                Detail(getDetailCount()-1).setSourceCode(loCachePayable.Master().getSourceCode());
//                Detail(getDetailCount()-1).setTransactionTotal(loCachePayable.Master().getNetTotal());
//                Detail(getDetailCount()-1).setDebitAmount(loCachePayable.Master().getPayableAmount());
//                Detail(getDetailCount()-1).setDebitAmount(loCachePayable.Master().getReceivableAmount());
//                Detail(getDetailCount()-1).setPayableType(payableType);
//                AddDetail();
                break;
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
        
        if (paDetailRemoved == null) {
            paDetailRemoved = new ArrayList<>();
        }
        
        Master().setModifyingId(poGRider.getUserID());
        Master().setModifiedDate(poGRider.getServerDate());
        
        if (getDetailCount() == 1) {
            //do not allow a detail with no applied amount
            if (Detail(0).getAppliedAmount().doubleValue() == 0.0000) {
                poJSON.put("result", "error");
                poJSON.put("message", "Invalid transaction total amount.");
                return poJSON;
            }
        }
        
        Iterator<Model> detail = Detail().iterator();
        String lsAppliedAmount = "0.0000";
        while (detail.hasNext()) {
            Model item = detail.next();
            if(item.getValue("nAppliedx") != null && !"".equals(item.getValue("nAppliedx"))){
                lsAppliedAmount = item.getValue("nAppliedx").toString();
            }
            if ( Double.valueOf(lsAppliedAmount) <= 0.0000) {
                detail.remove();
                
                if(item.getEditMode() == EditMode.UPDATE){
                    paDetailRemoved.add(item);
                }
                
            }
            lsAppliedAmount = "0.0000";
        }
        
        //Validate detail after removing all zero qty and empty stock Id
        if (getDetailCount() <= 0) {
            poJSON.put("result", "error");
            poJSON.put("message", "No SOA detail to be save.");
            return poJSON;
        }
        
        //assign other info on detail
        for (int lnCtr = 0; lnCtr <= getDetailCount() - 1; lnCtr++) {
            //Set value to por detail
            Detail(lnCtr).setTransactionNo(Master().getTransactionNo());
            Detail(lnCtr).setEntryNo(lnCtr + 1);
//            Detail(lnCtr).setModifiedDate(poGRider.getServerDate());
        }
        
        //Update linked transactions
        poJSON = setValueToOthers(Master().getTransactionStatus());
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        poJSON.put("result", "success");
        return poJSON;
    }
    
    @Override
    public JSONObject saveOthers() {
        try {
            /*Only modify this if there are other tables to modify except the master and detail tables*/
            poJSON = new JSONObject();
            
            poJSON = saveUpdateOthers(Master().getTransactionStatus());
            if (!"success".equals((String) poJSON.get("result"))) {
                return poJSON;
            }
            
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(SOATagging.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            poJSON.put("result", "error");
            poJSON.put("message", MiscUtil.getException(ex));
            return poJSON;
        }
        
        poJSON.put("result", "success");
        return poJSON;
    }
    @Override
    public JSONObject save() {
        /*Put saving business rules here*/
        return isEntryOkay(SOATaggingStatus.OPEN);
    }
    
    private PaymentRequest PaymentRequest() throws SQLException, GuanzonException{
        return new GLControllers(poGRider, logwrapr).PaymentRequest();
    }
    
    private JSONObject setValueToOthers(String status)
            throws CloneNotSupportedException,
            SQLException,
            GuanzonException {
        poJSON = new JSONObject();
        paPaymentRequest = new ArrayList<>();
        paCachePayable = new ArrayList<>();
        int lnCtr;

        //Update Purchase Order exist in PO Receiving Detail
        for (lnCtr = 0; lnCtr <= getDetailCount() - 1; lnCtr++) {
            System.out.println("----------------------SOA Detail---------------------- ");
            System.out.println("Source No : " + (lnCtr + 1) + " : " + Detail(lnCtr).getSourceNo());
            System.out.println("Payable Type : " + (lnCtr + 1) + " : " + Detail(lnCtr).getPayableType());
            System.out.println("Source Code : " + (lnCtr + 1) + " : " + Detail(lnCtr).getSourceCode());
            System.out.println("------------------------------------------------------------------ ");
            
            switch(Detail(lnCtr).getPayableType()){
                case SOATaggingStatic.PaymentRequest:
                    paPaymentRequest.add(PaymentRequest());
                    paPaymentRequest.get(paPaymentRequest.size()-1).InitTransaction();
                    paPaymentRequest.get(paPaymentRequest.size()-1).OpenTransaction(Detail(lnCtr).getSourceNo());
                    paPaymentRequest.get(paPaymentRequest.size()-1).UpdateTransaction();
                    paPaymentRequest.get(paPaymentRequest.size()-1).Master().setProcess("1");
                    
                    switch(status){
                        case SOATaggingStatus.VOID:
                        case SOATaggingStatus.CANCELLED:
                        case SOATaggingStatus.RETURNED:
                            paPaymentRequest.get(paPaymentRequest.size()-1).Master().setProcess("0");
//                            paPaymentRequest.get(paPaymentRequest.size()-1).Master().isProcessed(false);
                        break;
                        case SOATaggingStatus.CONFIRMED:
//                            paPaymentRequest.get(paPaymentRequest.size()-1).Master().isProcessed(true);
                        break;
                    }
                    
                    break;
                case SOATaggingStatic.CachePayable:
//                    paCachePayable.add(CachePayableMaster());
//                    paCachePayable.get(paCachePayable.size()-1).InitTransaction();
//                    paCachePayable.get(paCachePayable.size()-1).OpenTransaction(Detail(lnCtr).getSourceNo());
//                    paCachePayable.get(paCachePayable.size()-1).UpdateTransaction();
//                    paCachePayable.get(paCachePayable.size()-1).isProcessed(true);
//                    switch(status){
//                        case SOATaggingStatus.VOID:
//                        case SOATaggingStatus.CANCELLED:
//                        case SOATaggingStatus.RETURNED:
//                            paCachePayable.get(paCachePayable.size()-1).isProcessed(false);
//                            paCachePayable.get(paCachePayable.size()-1).isWithSOA(false);
//                        break;
//                        case SOATaggingStatus.CONFIRMED:
//                            paCachePayable.get(paCachePayable.size()-1).isWithSOA(true);
//                        break;
//                    }
                    break;
            }
            
        }

        //Update purchase order removed in purchase order receiving
        for (lnCtr = 0; lnCtr <= getDetailRemovedCount() - 1; lnCtr++) {
            
            System.out.println("----------------------Removed SOA Detail---------------------- ");
            System.out.println("Source No : " + (lnCtr + 1) + " : " + Detail(lnCtr).getSourceNo());
            System.out.println("Payable Type : " + (lnCtr + 1) + " : " + Detail(lnCtr).getPayableType());
            System.out.println("Source Code : " + (lnCtr + 1) + " : " + Detail(lnCtr).getSourceCode());
            System.out.println("------------------------------------------------------------------ ");
            
            switch(Detail(lnCtr).getPayableType()){
                case SOATaggingStatic.PaymentRequest:
                    paPaymentRequest.add(PaymentRequest());
                    paPaymentRequest.get(paPaymentRequest.size()-1).InitTransaction();
                    paPaymentRequest.get(paPaymentRequest.size()-1).OpenTransaction(Detail(lnCtr).getSourceNo());
                    paPaymentRequest.get(paPaymentRequest.size()-1).UpdateTransaction();
                    paPaymentRequest.get(paPaymentRequest.size()-1).Master().setProcess("0");
                    break;
                case SOATaggingStatic.CachePayable:
//                    paCachePayable.add(CachePayableMaster());
//                    paCachePayable.get(paCachePayable.size()-1).initialize();
//                    paCachePayable.get(paCachePayable.size()-1).openRecord(Detail(lnCtr).getSourceNo());
//                    paCachePayable.get(paCachePayable.size()-1).updateRecord();
//                    paCachePayable.get(paCachePayable.size()-1).isProcessed(false);
                    break;
            }
        }

        poJSON.put("result", "success");
        return poJSON;
    }

    private JSONObject saveUpdateOthers(String status)
            throws CloneNotSupportedException {
        /*Only modify this if there are other tables to modify except the master and detail tables*/
        poJSON = new JSONObject();
        int lnCtr;
        try {

            //1. Save Update Payment Request
            for (lnCtr = 0; lnCtr <= paPaymentRequest.size() - 1; lnCtr++) {
                paPaymentRequest.get(lnCtr).setWithParent(true);
                paPaymentRequest.get(lnCtr).Master().setModifiedDate(poGRider.getServerDate());
                poJSON = paPaymentRequest.get(lnCtr).SaveTransaction();
                if ("error".equals((String) poJSON.get("result"))) {
                    System.out.println("Save Payment Request " + (String) poJSON.get("message"));
                    return poJSON;
                }
            }

            //1. Save Update Payment Request
//            for (lnCtr = 0; lnCtr <= paCachePayable.size() - 1; lnCtr++) {
//                paCachePayable.get(lnCtr).setWithParent(true);
//                poJSON = paCachePayable.get(lnCtr).SaveTransaction();
//                if ("error".equals((String) poJSON.get("result"))) {
//                    System.out.println("Save Cache Payable " + (String) poJSON.get("message"));
//                    return poJSON;
//                }
//            }

        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(SOATagging.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            poJSON.put("result", "error");
            poJSON.put("message", MiscUtil.getException(ex));
            return poJSON;
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
        try {
            /*Put initial model values here*/
            poJSON = new JSONObject();
            System.out.println("Dept ID : " + poGRider.getDepartment());
            Master().setBranchCode(poGRider.getBranchCode());
            Master().setIndustryId(psIndustryId);
//            Master().setCompanyId(psCompanyId);
            Master().setTransactionDate(poGRider.getServerDate());
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
                + " , a.sClientID  "
                + " , a.sSOANoxxx  "
                + " , b.sCompnyNm  AS sSupplrNm"
                + " , c.sCompnyNm  AS sCompnyNm"
                + " , d.sDescript  AS sIndustry"
                + " FROM ap_payment_master a "
                + " LEFT JOIN client_master b ON b.sClientID = a.sClientID "
                + " LEFT JOIN company c ON c.sCompnyID = a.sCompnyID "
                + " LEFT JOIN industry d ON d.sIndstCdx = a.sIndstCdx ";
    }
    
    public String getPayableSQL() {
        return  " SELECT        "                                        
                + "   a.sTransNox "                                        
                + " , a.dTransact "                                        
                + " , a.sIndstCdx "                                        
                + " , a.cTranStat "                                        
                + " , a.nAmtPaidx "                                        
                + " , a.nNetTotal AS nPayblAmt  "                                        
                + " , b.sCompnyNm AS sPayablNm  "                                        
                + " ,  " + SQLUtil.toSQL(SOATaggingStatic.CachePayable) + " AS sPayablTp  "                         
                + " FROM cache_payable_master a "                          
                + " LEFT JOIN client_master b ON b.sClientID = a.sClientID "
                + " WHERE a.sIndstCdx = " + SQLUtil.toSQL(psIndustryId)
                + " AND a.cTranStat = "+ SQLUtil.toSQL(PaymentRequestStatus.CONFIRMED)
                + " AND a.nAmtPaidx < a.nNetTotal "
                + " UNION  "                                               
                + " SELECT "                                               
                + "   a.sTransNox "                                       
                + " , a.dTransact "                                       
                + " , a.sIndstCdx "                                       
                + " , a.cTranStat "                                       
                + " , a.nAmtPaidx "                                       
                + " , a.nTranTotl AS nPayblAmt    "                                       
                + " , b.sPayeeNme AS sPayablNm    "               
                + " ,  " + SQLUtil.toSQL(SOATaggingStatic.PaymentRequest) + " AS sPayablTp  "                       
                + " FROM payment_request_master a "                       
                + " LEFT JOIN payee b ON b.sPayeeIDx = a.sPayeeIDx "
                + " WHERE a.sIndstCdx = " + SQLUtil.toSQL(psIndustryId)
                + " AND a.cTranStat = "+ SQLUtil.toSQL(PaymentRequestStatus.CONFIRMED)
                + " AND a.nAmtPaidx < a.nTranTotl ";       
    }

    @Override
    protected JSONObject isEntryOkay(String status) {
        poJSON = new JSONObject();

        GValidator loValidator = new APPaymentValidator();
        loValidator.setApplicationDriver(poGRider);
        loValidator.setTransactionStatus(status);
        loValidator.setMaster(poMaster);
        poJSON = loValidator.validate();
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
