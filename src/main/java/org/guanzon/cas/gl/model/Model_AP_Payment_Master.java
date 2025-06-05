/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.guanzon.cas.gl.model;

import java.sql.SQLException;
import java.util.Date;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.Logical;
import org.guanzon.cas.client.model.Model_Client_Address;
import org.guanzon.cas.client.model.Model_Client_Institution_Contact;
import org.guanzon.cas.client.model.Model_Client_Master;
import org.guanzon.cas.client.services.ClientModels;
import org.guanzon.cas.gl.services.SOATaggingModels;
import org.guanzon.cas.gl.status.SOATaggingStatus;
import org.guanzon.cas.parameter.model.Model_Branch;
import org.guanzon.cas.parameter.model.Model_Category;
import org.guanzon.cas.parameter.model.Model_Company;
import org.guanzon.cas.parameter.model.Model_Industry;
import org.guanzon.cas.parameter.services.ParamModels;
//import org.guanzon.cas.purchasing.services.PurchaseOrderReceivingModels;
//import org.guanzon.cas.purchasing.status.SOATaggingStatus;
import org.json.simple.JSONObject;

/**
 *
 * @author Aldrich || Arsiela Team 2 05-23-2025
 */
public class Model_AP_Payment_Master extends Model {

    //reference objects
    Model_Branch poBranch;
    Model_Industry poIndustry;
    Model_Category poCategory;
    Model_Company poCompany;
    Model_Client_Master poSupplier;
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
            poEntity.updateObject("nEntryNox", 0);
            poEntity.updateObject("nTranTotl", 0.0000);
            poEntity.updateObject("nTranTotl", 0.0000);
            poEntity.updateObject("nDiscAmnt", 0.0000);
            poEntity.updateObject("nAmtPaidX", 0.0000);
            poEntity.updateObject("nTaxAmntx", 0.0000);
            poEntity.updateObject("nNetTotal", 0.0000);
            poEntity.updateObject("nFreightx", 0.00);
            poEntity.updateObject("nVATAmtxx", 0.00);
            poEntity.updateObject("nVatExmpt", 0.00);
            poEntity.updateObject("nZeroRted", 0.00);
            poEntity.updateString("cTranStat", SOATaggingStatus.OPEN);
            //end - assign default values

            poEntity.insertRow();
            poEntity.moveToCurrentRow();
            poEntity.absolute(1);

            ID = "sTransNox";

            //initialize reference objects
            ParamModels model = new ParamModels(poGRider);
            poBranch = model.Branch();
            poIndustry = model.Industry();
            poCategory = model.Category();
            poCompany = model.Company();

            ClientModels clientModel = new ClientModels(poGRider);
            poSupplier = clientModel.ClientMaster();
            poSupplierAdress = clientModel.ClientAddress();
            poSupplierContactPerson = clientModel.ClientInstitutionContact();
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
        return setValue("sBranchCD", branchCode);
    }

    public String getBranchCode() {
        return (String) getValue("sBranchCD");
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

    public JSONObject setSOANumber(String soaNumber) {
        return setValue("sSOANoxxx", soaNumber);
    }

    public String getSOANumber() {
        return (String) getValue("sSOANoxxx");
    }

    public JSONObject setIssuedTo(String issuedTo) {
        return setValue("sIssuedTo", issuedTo);
    }

    public String getIssuedTo() {
        return (String) getValue("sIssuedTo");
    }

    public JSONObject setRemarks(String remarks) {
        return setValue("sRemarksx", remarks);
    }

    public String getRemarks() {
        return (String) getValue("sRemarksx");
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
    
    public JSONObject setVATAmt(Number vatAmount) {
        return setValue("nVATAmtxx", vatAmount);
    }

    public Number getVATAmt() {
        if (getValue("nVATAmtxx") == null || "".equals(getValue("nVATAmtxx"))) {
            return 0.00;
        }
        return (Number) getValue("nVATAmtxx");
    }

    public JSONObject setVATExempt(Number vatExempt) {
        return setValue("nVatExmpt", vatExempt);
    }

    public Number getVATExempt() {
        if (getValue("nVatExmpt") == null || "".equals(getValue("nVatExmpt"))) {
            return 0.00;
        }
        return (Number) getValue("nVatExmpt");
    }

    public JSONObject setZeroVATSale(Number zeroVATSale) {
        return setValue("nZroVATSl", zeroVATSale);
    }

    public Number getZeroVATSale() {
        if (getValue("nZroVATSl") == null || "".equals(getValue("nZroVATSl"))) {
            return 0.00;
        }
        return (Number) getValue("nZroVATSl");
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

    public JSONObject setAmountPaid(Number amountPaid) {
        return setValue("nAmtPaidX", amountPaid);
    }

    public Number getAmountPaid() {
        if (getValue("nAmtPaidX") == null || "".equals(getValue("nAmtPaidX"))) {
            return 0.0000;
        }
        return (Number) getValue("nAmtPaidX");
    }

    public JSONObject setEntryNo(Number entryNo) {
        return setValue("nEntryNox", entryNo);
    }

    public Number getEntryNo() {
        return (Number) getValue("nEntryNox");
    }

    public JSONObject setTransactionStatus(String transactionStatus) {
        return setValue("cTranStat", transactionStatus);
    }

    public String getTransactionStatus() {
        return (String) getValue("cTranStat");
    }
    
    public JSONObject isProcessed(boolean isProcessed) {
        return setValue("cProcessd", isProcessed ? "1" : "0");
    }

    public boolean isProcessed() {
        return ((String) getValue("cProcessd")).equals("1");
    }

    public JSONObject setModifyingId(String modifiedBy) {
        return setValue("sModified", modifiedBy);
    }

    public String getModifyingId() {
        return (String) getValue("sModified");
    }

    public JSONObject setModifiedDate(Date modifiedDate) {
        return setValue("dModified", modifiedDate);
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

    public Model_Category Category() throws SQLException, GuanzonException {
        if (!"".equals((String) getValue("sCategrCd"))) {
            if (poCategory.getEditMode() == EditMode.READY
                    && poCategory.getCategoryId().equals((String) getValue("sCategrCd"))) {
                return poCategory;
            } else {
                poJSON = poCategory.openRecord((String) getValue("sCategrCd"));

                if ("success".equals((String) poJSON.get("result"))) {
                    return poCategory;
                } else {
                    poCategory.initialize();
                    return poCategory;
                }
            }
        } else {
            poCategory.initialize();
            return poCategory;
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

//    public Model_Client_Address SupplierAddress() throws SQLException, GuanzonException {
//        if (!"".equals((String) getValue("sAddressID"))) {
//            if (poSupplierAdress.getEditMode() == EditMode.READY
//                    && poSupplierAdress.getClientId().equals((String) getValue("sAddressID"))) {
//                return poSupplierAdress;
//            } else {
//                poJSON = poSupplierAdress.openRecord((String) getValue("sAddressID"));
//
//                if ("success".equals((String) poJSON.get("result"))) {
//                    return poSupplierAdress;
//                } else {
//                    poSupplierAdress.initialize();
//                    return poSupplierAdress;
//                }
//            }
//        } else {
//            poSupplierAdress.initialize();
//            return poSupplierAdress;
//        }
//    }

    public Model_Client_Institution_Contact SupplierContactPerson() throws SQLException, GuanzonException {
        if (!"".equals((String) getValue("sContctID"))) {
            if (poSupplierContactPerson.getEditMode() == EditMode.READY
                    && poSupplierContactPerson.getClientId().equals((String) getValue("sContctID"))) {
                return poSupplierContactPerson;
            } else {
                poJSON = poSupplierContactPerson.openRecord((String) getValue("sContctID"));

                if ("success".equals((String) poJSON.get("result"))) {
                    return poSupplierContactPerson;
                } else {
                    poSupplierContactPerson.initialize();
                    return poSupplierContactPerson;
                }
            }
        } else {
            poSupplierContactPerson.initialize();
            return poSupplierContactPerson;
        }
    }
    //end - reference object models

}
