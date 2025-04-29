
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.cas.gl.Particular;
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
public class testParticular {
    
    static GRiderCAS instance;
    static Particular poGLControllers;
    static JSONObject poJSON;

    @BeforeClass
    public static void setUpClass() {
        System.setProperty("sys.default.path.metadata", "D:/GGC_Maven_Systems/config/metadata/new/");

        instance = MiscUtil.Connect();
        
        poGLControllers = new Particular();
        poGLControllers.setApplicationDriver(instance);
        poGLControllers.setWithParentClass(false);
        poGLControllers.initialize();
    }

    @Test
    public void testNewRecord() {

        try {
            poJSON = poGLControllers.getModel().setDescription("CENPELCO");
            if ("error".equals((String) poJSON.get("result"))) {
                Assert.fail((String) poJSON.get("message"));
            }

            poJSON = poGLControllers.getModel().setAccountCode("0000001");
            if ("error".equals((String) poJSON.get("result"))) {
                Assert.fail((String) poJSON.get("message"));
            }
            poJSON = poGLControllers.saveRecord();
            if ("error".equals((String) poJSON.get("result"))) {
                Assert.fail((String) poJSON.get("message"));
            }
        } catch (SQLException | GuanzonException | CloneNotSupportedException e) {
            Assert.fail(e.getMessage());
        }
    }

//    @Test
//    public void testUpdateRecord() {
//        try {
//            JSONObject poJSON;
//            poJSON = poGLControllers.openRecord("24009");
//            if ("error".equals((String) poJSON.get("result"))) {
//                Assert.fail((String) poJSON.get("message"));
//            }
//
//            poJSON = poGLControllers.updateRecord();
//            if ("error".equals((String) poJSON.get("result"))) {
//                Assert.fail((String) poJSON.get("message"));
//            }
//            poJSON = poGLControllers.getModel().setParticularName("CENPELCO");
//            if ("error".equals((String) poJSON.get("result"))) {
//                Assert.fail((String) poJSON.get("message"));
//            }
//
//            poJSON = poGLControllers.getModel().setParticularID("0001");
//            if ("error".equals((String) poJSON.get("result"))) {
//                Assert.fail((String) poJSON.get("message"));
//            }
//            poJSON = poGLControllers.getModel().setModifyingId(oApp.getUserID());
//            if ("error".equals((String) poJSON.get("result"))) {
//                Assert.fail((String) poJSON.get("message"));
//            }
//
//            poJSON = poGLControllers.getModel().setModifiedDate(oApp.getServerDate());
//            if ("error".equals((String) poJSON.get("result"))) {
//                Assert.fail((String) poJSON.get("message"));
//            }
//
//            poJSON = poGLControllers.saveRecord();
//            if ("error".equals((String) poJSON.get("result"))) {
//                Assert.fail((String) poJSON.get("message"));
//            }
//        } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
//            Logger.getLogger(testParticular.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }

    @AfterClass
    public static void tearDownClass() {
        poGLControllers = null;
        instance = null;
    }
    
}
