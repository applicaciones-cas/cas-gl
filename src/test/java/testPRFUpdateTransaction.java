
import java.sql.SQLException;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.cas.gl.services.GLControllers;
import org.json.simple.JSONObject;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class testPRFUpdateTransaction {

    
    static GRiderCAS poApp;
    static GLControllers poPaymentRequest;

    @BeforeClass
    public static void setUpClass() {
        System.setProperty("sys.default.path.metadata", "D:/GGC_Maven_Systems/config/metadata/new/");

        poApp = MiscUtil.Connect();

        poPaymentRequest = new GLControllers(poApp, null);
    }
    @Test
    public void testUpdateTransaction() throws GuanzonException {
        JSONObject loJSON;

        try {
            loJSON = (JSONObject) poPaymentRequest.PaymentRequest().InitTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            loJSON = (JSONObject) poPaymentRequest.PaymentRequest().OpenTransaction("GCC225000001");
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            loJSON = (JSONObject) poPaymentRequest.PaymentRequest().UpdateTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            poPaymentRequest.PaymentRequest().Detail(1).setParticularID("M001250002");
            poPaymentRequest.PaymentRequest().Detail(2).setParticularID("M001250003");
            poPaymentRequest.PaymentRequest().Detail(0).setAmount(1000);
            poPaymentRequest.PaymentRequest().Detail(1).setAmount(2000);
            poPaymentRequest.PaymentRequest().Detail(2).setAmount(3000);
            
            poPaymentRequest.PaymentRequest().Detail(0).setModifiedDate(poApp.getServerDate());
            poPaymentRequest.PaymentRequest().Detail(1).setModifiedDate(poApp.getServerDate());
            poPaymentRequest.PaymentRequest().Detail(2).setModifiedDate(poApp.getServerDate());
            
            loJSON = poPaymentRequest.PaymentRequest().SaveTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
        } catch (CloneNotSupportedException | SQLException e) {
            System.err.println(MiscUtil.getException(e));
            Assert.fail();
        }

    }    @AfterClass
    public static void tearDownClass() {
        poPaymentRequest = null;
        poApp = null;
    }
}
