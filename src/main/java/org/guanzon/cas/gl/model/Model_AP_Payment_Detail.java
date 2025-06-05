/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.guanzon.cas.gl.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.cas.inv.model.Model_Inv_Serial;
import org.guanzon.cas.inv.model.Model_Inv_Serial_Registration;
import org.guanzon.cas.inv.model.Model_Inventory;
import org.guanzon.cas.inv.services.InvModels;
import org.json.simple.JSONObject;

/**
 *
 * @author User
 */
public class Model_AP_Payment_Detail extends Model {

    Number psReceiveQty = 1;

    //reference objects
    Model_Inventory poInventory;
    Model_Inv_Serial poInvSerial;
    Model_Payment_Request_Master poPaymentRequest;
    Model_Inv_Serial_Registration poInvSerialRegistration;

    @Override
    public void initialize() {
        try {
            poEntity = MiscUtil.xml2ResultSet(System.getProperty("sys.default.path.metadata") + XML, getTable());

            poEntity.last();
            poEntity.moveToInsertRow();

            MiscUtil.initRowSet(poEntity);

            //assign default values
            poEntity.updateObject("dModified", SQLUtil.toDate("1900-01-01", SQLUtil.FORMAT_SHORT_DATE));
            poEntity.updateObject("nEntryNox", 0);
            poEntity.updateObject("nDebitAmt", 0.00);
            poEntity.updateObject("nCredtAmt", 0.00);
            poEntity.updateObject("nVatRatex", 0.00);
            poEntity.updateObject("nVATAmtxx", 0.00);
            poEntity.updateObject("nNonVATSl", 0.00);
            poEntity.updateObject("nZroVATSl", 0.00);
            poEntity.updateObject("nTWithHld", 0.00);
            //end - assign default values

            poEntity.insertRow();
            poEntity.moveToCurrentRow();
            poEntity.absolute(1);

            ID = "sTransNox";
            ID2 = "nEntryNox";

            //initialize reference objects
            InvModels invModel = new InvModels(poGRider);
            poInventory = invModel.Inventory();
            poInvSerial = invModel.InventorySerial();
            poInvSerialRegistration = invModel.InventorySerialRegistration();
            //end - initialize reference objects

            pnEditMode = EditMode.UNKNOWN;
        } catch (SQLException e) {
            logwrapr.severe(e.getMessage());
            System.exit(1);
        }
    }

    public JSONObject setTransactionNo(String transactionNo) {
        return setValue("sTransNox", transactionNo);
    }

    public String getTransactionNo() {
        return (String) getValue("sTransNox");
    }

    public JSONObject setEntryNo(Number entryNo) {
        return setValue("nEntryNox", entryNo);
    }

    public Number getEntryNo() {
        return (Number) getValue("nEntryNox");
    }

    public JSONObject setSourceNo(String sourceNo) {
        return setValue("sSourceNo", sourceNo);
    }

    public String getSourceNo() {
        return (String) getValue("sSourceNo");
    }

    public JSONObject setSourceCode(String sourceCode) {
        return setValue("sSourceCd", sourceCode);
    }

    public String getSourceCode() {
        return (String) getValue("sSourceCd");
    }

    public JSONObject setDebitAmount(Number debitAmount) {
        return setValue("nDebitAmt", debitAmount);
    }

    public Number getDebitAmount() {
        return (Number) getValue("nDebitAmt");
    }

    public JSONObject setCreditAmount(Number creditAmount) {
        return setValue("nCredtAmt", creditAmount);
    }

    public Number getCreditAmount() {
        return (Number) getValue("nCredtAmt");
    }

    public JSONObject setAppliedAmount(Number appliedAmount) {
        return setValue("nAppliedx", appliedAmount);
    }

    public Number getAppliedAmount() {
        return (Number) getValue("nAppliedx");
    }

    public JSONObject setVATAmount(Number vatAmount) {
        return setValue("nVATAmtxx", vatAmount);
    }

    public Number getVATAmount() {
        return (Number) getValue("nVATAmtxx");
    }

    public JSONObject setNonVATSale(Number nonVATSale) {
        return setValue("nNonVATSl", nonVATSale);
    }

    public Number getNonVATSale() {
        return (Number) getValue("nNonVATSl");
    }

    public JSONObject setVATExempt(Number vatExempt) {
        return setValue("nVatExmpt", vatExempt);
    }

    public Number getVATExempt() {
        return (Number) getValue("nVatExmpt");
    }

    public JSONObject setZeroVATSale(Number zeroVATSale) {
        return setValue("nZroVATSl", zeroVATSale);
    }

    public Number getZeroVATSale() {
        return (Number) getValue("nZroVATSl");
    }

    public JSONObject setTaxAmount(Number taxAmount) {
        return setValue("nTaxAmntx", taxAmount);
    }

    public Number getTaxAmount() {
        return (Number) getValue("nTaxAmntx");
    }

    public JSONObject setModifiedDate(Date modifiedDate) {
        return setValue("dModified", modifiedDate);
    }

    public Date getModifiedDate() {
        return (Date) getValue("dModified");
    }

    @Override
    public String getNextCode() {
        return "";
    }

    //reference object models
//    public Model_Inventory Inventory() throws SQLException, GuanzonException {
//        if (!"".equals((String) getValue("sStockIDx"))) {
//            if (poInventory.getEditMode() == EditMode.READY
//                    && poInventory.getStockId().equals((String) getValue("sStockIDx"))) {
//                return poInventory;
//            } else {
//                poJSON = poInventory.openRecord((String) getValue("sStockIDx"));
//
//                if ("success".equals((String) poJSON.get("result"))) {
//                    return poInventory;
//                } else {
//                    poInventory.initialize();
//                    return poInventory;
//                }
//            }
//        } else {
//            poInventory.initialize();
//            return poInventory;
//        }
//    }
//    public Model_Inv_Serial InventorySerial() throws SQLException, GuanzonException {
//        if (!"".equals((String) getValue("sSerialID"))) {
//            if (poInvSerial.getEditMode() == EditMode.READY
//                    && poInvSerial.getSerialId().equals((String) getValue("sSerialID"))) {
//                return poInvSerial;
//            } else {
//                poJSON = poInvSerial.openRecord((String) getValue("sSerialID"));
//
//                if ("success".equals((String) poJSON.get("result"))) {
//                    return poInvSerial;
//                } else {
//                    poInvSerial.initialize();
//                    return poInvSerial;
//                }
//            }
//        } else {
//            poInvSerial.initialize();
//            return poInvSerial;
//        }
//    }
//    public Model_Inv_Serial_Registration InventorySerialRegistration() throws SQLException, GuanzonException {
//        if (!"".equals((String) getValue("sSerialID"))) {
//            if (poInvSerialRegistration.getEditMode() == EditMode.READY
//                    && poInvSerialRegistration.getSerialId().equals((String) getValue("sSerialID"))) {
//                return poInvSerialRegistration;
//            } else {
//                poJSON = poInvSerialRegistration.openRecord((String) getValue("sSerialID"));
//
//                if ("success".equals((String) poJSON.get("result"))) {
//                    return poInvSerialRegistration;
//                } else {
//                    poInvSerialRegistration.initialize();
//                    return poInvSerialRegistration;
//                }
//            }
//        } else {
//            poInvSerialRegistration.initialize();
//            return poInvSerialRegistration;
//        }
//    }
    public Model_Payment_Request_Master PaymentRequestMaster() throws SQLException, GuanzonException {
        if (!"".equals((String) getValue("sSourceCd"))) {
            if (poPaymentRequest.getEditMode() == EditMode.READY
                    && poPaymentRequest.getTransactionNo().equals((String) getValue("sSourceCd"))) {
                return poPaymentRequest;
            } else {
                poJSON = poPaymentRequest.openRecord((String) getValue("sOrderNox"));

                if ("success".equals((String) poJSON.get("result"))) {
                    return poPaymentRequest;
                } else {
                    poPaymentRequest.initialize();
                    return poPaymentRequest;
                }
            }
        } else {
            poPaymentRequest.initialize();
            return poPaymentRequest;
        }
    }

    public JSONObject openRecord(String transactionNo, String stockId) throws SQLException, GuanzonException {
        poJSON = new JSONObject();
        String lsSQL = MiscUtil.makeSelect(this);
        lsSQL = MiscUtil.addCondition(lsSQL, " sTransNox = " + SQLUtil.toSQL(transactionNo)
                + " AND sStockIDx = " + SQLUtil.toSQL(stockId));
        System.out.println("Executing SQL: " + lsSQL);
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
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", e.getMessage());
        }
        return poJSON;
    }
    //end reference object models
}
