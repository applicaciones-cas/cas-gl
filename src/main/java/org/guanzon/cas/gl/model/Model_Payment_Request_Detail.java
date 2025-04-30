/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.guanzon.cas.gl.model;

import java.sql.SQLException;
import java.util.Date;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.Logical;
import org.guanzon.cas.gl.services.GLModels;
import org.guanzon.cas.gl.status.PaymentRequestStatus;
import org.guanzon.cas.parameter.services.ParamModels;
import org.json.simple.JSONObject;

/**
 *
 * @author User
 */
public class Model_Payment_Request_Detail extends Model {

    Model_Particular poParticular;

    @Override
    public void initialize() {
        try {
            poEntity = MiscUtil.xml2ResultSet(System.getProperty("sys.default.path.metadata") + XML, getTable());

            poEntity.last();
            poEntity.moveToInsertRow();

            MiscUtil.initRowSet(poEntity);

            //assign default values
            poEntity.updateObject("nEntryNox", 0);
            poEntity.updateObject("nAmountxx", 0.0);
            poEntity.updateObject("nDiscount", 0.0);
            poEntity.updateObject("nAddDiscx", 0.0);
            poEntity.updateObject("nTWithHld", 0.0);

            //end - assign default values
            poEntity.insertRow();
            poEntity.moveToCurrentRow();

            poEntity.absolute(1);

            ID = "sTransNox";
            ID2 = "nEntryNox";

            GLModels gl = new GLModels(poGRider);
            poParticular = gl.Particular();

            //end - initialize reference objects
            pnEditMode = EditMode.UNKNOWN;
        } catch (SQLException e) {
            logwrapr.severe(e.getMessage());
            System.exit(1);
        }
    }

    @Override
    public String getNextCode() {
        return "";
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

    public JSONObject setParticularID(String particularID) {
        return setValue("sPrtclrID", particularID);
    }

    public String getParticularID() {
        return (String) getValue("sPrtclrID");
    }

    public JSONObject setPRFRemarks(String prfRemarks) {
        return setValue("sPRFRemxx", prfRemarks);
    }

    public String getPRFRemarks() {
        return (String) getValue("sPRFRemxx");
    }

    public JSONObject setAmount(Number amount) {
        return setValue("nAmountxx", amount);
    }

    public Number getAmount() {
        return (Number) getValue("nAmountxx");
    }

    public JSONObject setDiscount(Number discount) {
        return setValue("nDiscount", discount);
    }

    public Number getDiscount() {
        return (Number) getValue("nDiscount");
    }

    public JSONObject setAddDiscount(Number addDiscount) {
        return setValue("nAddDiscx", addDiscount);
    }

    public Number getAddDiscount() {
        return (Number) getValue("nAddDiscx");
    }

    public JSONObject setVatable(String vatable) {
        return setValue("cVATaxabl", vatable);
    }

    public String getVatable() {
        return (String) getValue("cVATaxabl");
    }

    public JSONObject setWithHoldingTax(Number withHoldingTax) {
        return setValue("nTWithHld", withHoldingTax);
    }

    public Number getWithHoldingTax() {
        return (Number) getValue("nTWithHld");
    }

    public JSONObject setModifiedDate(Date modifiedDate) {
        return setValue("dModified", modifiedDate);
    }

    public Date getModifiedDate() {
        return (Date) getValue("dModified");
    }

    public Model_Particular Particular() throws GuanzonException, SQLException {
        if (!"".equals((String) getValue("sPrtclrID"))) {
            if (poParticular.getEditMode() == EditMode.READY
                    && poParticular.getParticularID().equals((String) getValue("sPrtclrID"))) {
                return poParticular;
            } else {
                poJSON = poParticular.openRecord((String) getValue("sPrtclrID"));
                if ("success".equals((String) poJSON.get("result"))) {
                    return poParticular;
                } else {
                    poParticular.initialize();
                    return poParticular;
                }
            }
        } else {
            poParticular.initialize();
            return poParticular;
        }
    }
}
