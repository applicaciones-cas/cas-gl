
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.cas.gl.Payee;
import org.guanzon.cas.gl.services.GLControllers;
import org.json.simple.JSONObject;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/**
 *
 * @author User
 */
public class testPayee {

    static GRiderCAS oApp;
    static GLControllers poGLControllers;
    static JSONObject poJSON;
    static Payee record;

    @BeforeClass
    public static void setUpClass() {
        try {
            System.setProperty("sys.default.path.metadata", "D:/GGC_Maven_Systems/config/metadata/new/");

            oApp = MiscUtil.Connect();
//        poGLControllers = new GLControllers(oApp, null);
//
            GLControllers ctrl = new GLControllers(oApp, null);
            record = ctrl.Payee();
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(testPayee.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Test
    public void testNewRecord() {

        try {
//            poJSON = record.newRecord();
//            if ("error".equals((String) poJSON.get("result"))) {
//                Assert.fail((String) poJSON.get("message"));
//            }
            poJSON = record.getModel().setPayeeName("CENPELCO");
            if ("error".equals((String) poJSON.get("result"))) {
                Assert.fail((String) poJSON.get("message"));
            }

            poJSON = record.getModel().setParticularID("0001");
            if ("error".equals((String) poJSON.get("result"))) {
                Assert.fail((String) poJSON.get("message"));
            }
            poJSON = record.saveRecord();
            if ("error".equals((String) poJSON.get("result"))) {
                Assert.fail((String) poJSON.get("message"));
            }
        } catch (SQLException | GuanzonException | CloneNotSupportedException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testUpdateRecord() {
        try {
            JSONObject poJSON;
            poJSON = record.openRecord("24009");
            if ("error".equals((String) poJSON.get("result"))) {
                Assert.fail((String) poJSON.get("message"));
            }

            poJSON = record.updateRecord();
            if ("error".equals((String) poJSON.get("result"))) {
                Assert.fail((String) poJSON.get("message"));
            }
            poJSON = record.getModel().setPayeeName("CENPELCO");
            if ("error".equals((String) poJSON.get("result"))) {
                Assert.fail((String) poJSON.get("message"));
            }

            poJSON = record.getModel().setParticularID("0001");
            if ("error".equals((String) poJSON.get("result"))) {
                Assert.fail((String) poJSON.get("message"));
            }
            poJSON = record.getModel().setModifyingId(oApp.getUserID());
            if ("error".equals((String) poJSON.get("result"))) {
                Assert.fail((String) poJSON.get("message"));
            }

            poJSON = record.getModel().setModifiedDate(oApp.getServerDate());
            if ("error".equals((String) poJSON.get("result"))) {
                Assert.fail((String) poJSON.get("message"));
            }

            poJSON = record.saveRecord();
            if ("error".equals((String) poJSON.get("result"))) {
                Assert.fail((String) poJSON.get("message"));
            }
        } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
            Logger.getLogger(testPayee.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @AfterClass
    public static void tearDownClass() {
        poGLControllers = null;
        oApp = null;
    }
}
