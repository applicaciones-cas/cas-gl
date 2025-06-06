/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.guanzon.cas.gl.model;

import java.util.Date;
import java.sql.SQLException;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.cas.client.model.Model_Client_Address;
import org.guanzon.cas.client.model.Model_Client_Institution_Contact;
import org.guanzon.cas.client.model.Model_Client_Master;
import org.guanzon.cas.client.services.ClientModels;
import org.guanzon.cas.gl.services.GLModels;
import org.guanzon.cas.gl.status.APPaymentAdjustmentStatus;
import org.guanzon.cas.parameter.model.Model_Branch;
import org.guanzon.cas.parameter.model.Model_Category;
import org.guanzon.cas.parameter.model.Model_Company;
import org.guanzon.cas.parameter.model.Model_Industry;
import org.guanzon.cas.parameter.services.ParamModels;
import org.json.simple.JSONObject;

/**
 *
 * @author Arsiela 06/05/2025
 */
public class Model_AP_Payment_Adjustment extends Model {

    Model_Branch poBranch;
    Model_Industry poIndustry;
    Model_Company poCompany;
    Model_Client_Master poSupplier;
    Model_Payee poPayee;
    Model_Client_Address poSupplierAdress;
    Model_Client_Institution_Contact poSupplierContactPerson;

    @Override
    public void initialize() {
        try {
            poEntity = MiscUtil.xml2ResultSet(System.getProperty("sys.default.path.metadata") + XML, getTable());

            poEntity.last();
            poEntity.moveToInsertRow();

            MiscUtil.initRowSet(poEntity);

            //assign default values
            poEntity.updateObject("dTransact", SQLUtil.toDate("1900-01-01", SQLUtil.FORMAT_SHORT_DATE));
            poEntity.updateObject("dModified", SQLUtil.toDate("1900-01-01", SQLUtil.FORMAT_SHORT_DATE));
            poEntity.updateObject("nDebitAmt", 0.0000);
            poEntity.updateObject("nCredtAmt", 0.0000);
            poEntity.updateObject("nAppliedx", 0.0000);
            poEntity.updateObject("nTranTotl", 0.0000);
            poEntity.updateObject("nFreightx", 0.0000);
            poEntity.updateObject("nDiscAmnt", 0.0000);
            poEntity.updateObject("nVATAmtxx", 0.0000);
            poEntity.updateObject("nVatExmpt", 0.0000);
            poEntity.updateObject("nZeroRted", 0.0000);
            poEntity.updateObject("nTaxAmntx", 0.0000);
            poEntity.updateObject("nNetTotal", 0.0000);
            poEntity.updateString("cTranStat", APPaymentAdjustmentStatus.OPEN);
            //end - assign default values

            poEntity.insertRow();
            poEntity.moveToCurrentRow();
            poEntity.absolute(1);

            ID = "sTransNox";

            //initialize reference objects
            ParamModels model = new ParamModels(poGRider);
            poBranch = model.Branch();
            poIndustry = model.Industry();
            poCompany = model.Company();

            ClientModels clientModel = new ClientModels(poGRider);

            poSupplier = clientModel.ClientMaster();
            poSupplierAdress = clientModel.ClientAddress();
            poSupplierContactPerson = clientModel.ClientInstitutionContact();

            GLModels gl = new GLModels(poGRider);
            poPayee = gl.Payee();
//            end - initialize reference objects

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

    public JSONObject setIndustryId(String industryId) {
        return setValue("sIndstCdx", industryId);
    }

    public String getIndustryId() {
        return (String) getValue("sIndstCdx");
    }

    public JSONObject setBranchCode(String branchCode) {
        return setValue("sBranchCd", branchCode);
    }

    public String getBranchCode() {
        return (String) getValue("sBranchCd");
    }

    public JSONObject setTransactionDate(Date transactionDate) {
        return setValue("dTransact", transactionDate);
    }

    public Date getTransactionDate() {
        return (Date) getValue("dTransact");
    }

    public JSONObject setCompanyId(String companyId) {
        return setValue("sCompnyID", companyId);
    }

    public String getCompanyId() {
        return (String) getValue("sCompnyID");
    }

    public JSONObject setClientId(String clientId) {
        return setValue("sClientID", clientId);
    }

    public String getClientId() {
        return (String) getValue("sClientID");
    }

    public JSONObject setIssuedTo(String issuedTo) {
        return setValue("sIssuedTo", issuedTo);
    }

    public String getIssuedTo() {
        return (String) getValue("sIssuedTo");
    }

    public JSONObject setReferenceNo(String referenceNo) {
        return setValue("sReferNox", referenceNo);
    }

    public String getReferenceNo() {
        return (String) getValue("sReferNox");
    }

    public JSONObject setPayerCode(String payerCode) {
        return setValue("cPayerCde", payerCode);
    }

    public String getPayerCode() {
        return (String) getValue("cPayerCde");
    }

    public JSONObject setRemarks(String remarks) {
        return setValue("sRemarksx", remarks);
    }

    public String getRemarks() {
        return (String) getValue("sRemarksx");
    }

    public JSONObject setSourceCode(String sourceCode) {
        return setValue("sSourceCd", sourceCode);
    }

    public String getSourceCode() {
        return (String) getValue("sSourceCd");
    }

    public JSONObject setSourceNo(String sourceNo) {
        return setValue("sSourceNo", sourceNo);
    }

    public String getSourceNo() {
        return (String) getValue("sSourceNo");
    }

    public JSONObject setDebitAmount(Number debitAmount) {
        return setValue("nDebitAmt", debitAmount);
    }

    public Number getDebitAmount() {
        if (getValue("nDebitAmt") == null || "".equals(getValue("nDebitAmt"))) {
            return 0.0000;
        }
        return (Number) getValue("nDebitAmt");
    }

    public JSONObject setCreditAmount(Number creditAmount) {
        return setValue("nCredtAmt", creditAmount);
    }

    public Number getCreditAmount() {
        if (getValue("nCredtAmt") == null || "".equals(getValue("nCredtAmt"))) {
            return 0.0000;
        }
        return (Number) getValue("nCredtAmt");
    }

    public JSONObject setAppliedAmount(Number appliedAmount) {
        return setValue("nAppliedx", appliedAmount);
    }

    public Number getAppliedAmount() {
        if (getValue("nAppliedx") == null || "".equals(getValue("nAppliedx"))) {
            return 0.0000;
        }
        return (Number) getValue("nAppliedx");
    }

    public JSONObject setTransactionTotal(Number transactionTotal) {
        return setValue("nTranTotl", transactionTotal);
    }

    public Number getTransactionTotal() {
        if (getValue("nTranTotl") == null || "".equals(getValue("nTranTotl"))) {
            return 0.0000;
        }
        return (Number) getValue("nTranTotl");
    }

    public JSONObject setFreightAmount(Number freightAmount) {
        return setValue("nFreightx", freightAmount);
    }

    public Number getFreightAmount() {
        if (getValue("nFreightx") == null || "".equals(getValue("nFreightx"))) {
            return 0.00;
        }
        return (Number) getValue("nFreightx");
    }

    public JSONObject setDiscountAmount(Number discountAmount) {
        return setValue("nDiscAmnt", discountAmount);
    }

    public Number getDiscountAmount() {
        if (getValue("nDiscAmnt") == null || "".equals(getValue("nDiscAmnt"))) {
            return 0.0000;
        }
        return (Number) getValue("nDiscAmnt");
    }

    public JSONObject setVATAmount(Number vatAmount) {
        return setValue("nVATAmtxx", vatAmount);
    }

    public Number getVATAmount() {
        if (getValue("nVATAmtxx") == null || "".equals(getValue("nVATAmtxx"))) {
            return 0.00;
        }
        return (Number) getValue("nVATAmtxx");
    }

    public JSONObject setVatExemptAmount(Number vatExemptAmount) {
        return setValue("nVatExmpt", vatExemptAmount);
    }

    public Number getVatExemptAmount() {
        if (getValue("nVatExmpt") == null || "".equals(getValue("nVatExmpt"))) {
            return 0.00;
        }
        return (Number) getValue("nVatExmpt");
    }

    public JSONObject setZeroRatedVatAmount(Number zeroRatedVatAmount) {
        return setValue("nZeroRted", zeroRatedVatAmount);
    }

    public Number getZeroRatedVatAmount() {
        if (getValue("nZeroRted") == null || "".equals(getValue("nZeroRted"))) {
            return 0.00;
        }
        return (Number) getValue("nZeroRted");
    }

    public JSONObject setTaxAmount(Number taxAmount) {
        return setValue("nTaxAmntx", taxAmount);
    }

    public Number getTaxAmount() {
        if (getValue("nTaxAmntx") == null || "".equals(getValue("nTaxAmntx"))) {
            return 0.0000;
        }
        return (Number) getValue("nTaxAmntx");
    }

    public JSONObject setNetTotal(Number netTotal) {
        return setValue("nNetTotal", netTotal);
    }

    public Number getNetTotal() {
        if (getValue("nNetTotal") == null || "".equals(getValue("nNetTotal"))) {
            return 0.0000;
        }
        return (Number) getValue("nNetTotal");
    }

    public JSONObject isProcessed(boolean isProcessed) {
        return setValue("cProcessd", isProcessed ? "1" : "0");
    }

    public boolean isProcessed() {
        return ((String) getValue("cProcessd")).equals("1");
    }

    public JSONObject setTransactionStatus(String transactionStatus) {
        return setValue("cTranStat", transactionStatus);
    }

    public String getTransactionStatus() {
        return (String) getValue("cTranStat");
    }

    public JSONObject setModifyingBy(String modified) {
        return setValue("sModified", modified);
    }

    public String getModifyingBy() {
        return (String) getValue("sModified");
    }

    public JSONObject setModifiedDate(Date modified) {
        return setValue("dModified", modified);
    }

    public Date getModifiedDate() {
        return (Date) getValue("dModified");
    }

    @Override
    public String getNextCode() {
//        return "";
        return MiscUtil.getNextCode(this.getTable(), ID, true, poGRider.getGConnection().getConnection(), poGRider.getBranchCode());
    }

    //reference object models
    public Model_Branch Branch() throws SQLException, GuanzonException {
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

    public Model_Industry Industry() throws SQLException, GuanzonException {
        if (!"".equals((String) getValue("sIndstCdx"))) {
            if (poIndustry.getEditMode() == EditMode.READY
                    && poIndustry.getIndustryId().equals((String) getValue("sIndstCdx"))) {
                return poIndustry;
            } else {
                poJSON = poIndustry.openRecord((String) getValue("sIndstCdx"));

                if ("success".equals((String) poJSON.get("result"))) {
                    return poIndustry;
                } else {
                    poIndustry.initialize();
                    return poIndustry;
                }
            }
        } else {
            poIndustry.initialize();
            return poIndustry;
        }
    }

    public Model_Company Company() throws SQLException, GuanzonException {
        if (!"".equals((String) getValue("sCompnyID"))) {
            if (poCompany.getEditMode() == EditMode.READY
                    && poCompany.getCompanyId().equals((String) getValue("sCompnyID"))) {
                return poCompany;
            } else {
                poJSON = poCompany.openRecord((String) getValue("sCompnyID"));

                if ("success".equals((String) poJSON.get("result"))) {
                    return poCompany;
                } else {
                    poCompany.initialize();
                    return poCompany;
                }
            }
        } else {
            poCompany.initialize();
            return poCompany;
        }
    }

    public Model_Client_Master Supplier() throws SQLException, GuanzonException {
        if (!"".equals((String) getValue("sClientID"))) {
            if (poSupplier.getEditMode() == EditMode.READY
                    && poSupplier.getClientId().equals((String) getValue("sClientID"))) {
                return poSupplier;
            } else {
                poJSON = poSupplier.openRecord((String) getValue("sClientID"));

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

    public Model_Payee Payee() throws SQLException, GuanzonException {
        if (!"".equals((String) getValue("sIssuedTo"))) {
            if (poPayee.getEditMode() == EditMode.READY
                    && poPayee.getPayeeID().equals((String) getValue("sIssuedTo"))) {
                return poPayee;
            } else {
                poJSON = poPayee.openRecord((String) getValue("sIssuedTo"));

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
}
