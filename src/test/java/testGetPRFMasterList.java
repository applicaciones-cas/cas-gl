
import java.sql.SQLException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
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
public class testGetPRFMasterList {

    static GRiderCAS poApp;
    static GLControllers poPaymentRequest;


    @BeforeClass
    public static void setUpClass() {
        try {
            System.setProperty("sys.default.path.metadata", "D:/GGC_Maven_Systems/config/metadata/new/");
            
            poApp = MiscUtil.Connect();
            
            poPaymentRequest = new GLControllers(poApp, null);
            poPaymentRequest.PaymentRequest().setTransactionStatus("0");
            
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(testGetPRFMasterList.class.getName()).log(Level.SEVERE, null, ex);
        } 
        
    }
    @Test
    public void testGetPurchaseOrder() {
        JSONObject loJSON;
        try {
            loJSON = poPaymentRequest.PaymentRequest().InitTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            
            loJSON = poPaymentRequest.PaymentRequest().getPaymentRequest("", "");

            if ("success".equals((String) loJSON.get("result"))) {
                System.out.println("RESULT" + (String) loJSON.get("message"));
                for (int lnCntr = 0; lnCntr <= poPaymentRequest.PaymentRequest().getPRFMasterCount() - 1; lnCntr++) {
                    System.out.println("poPurchasingController no:" + poPaymentRequest.PaymentRequest().poPRFMaster(lnCntr).getTransactionNo());
                    System.out.println("poPurchasingController entry no:" + poPaymentRequest.PaymentRequest().poPRFMaster(lnCntr).getEntryNo());
                    System.out.println("poPurchasingController status:" + poPaymentRequest.PaymentRequest().poPRFMaster(lnCntr).getTransactionStatus());
                }
            }
            
        } catch (ExceptionInInitializerError | SQLException | GuanzonException e) {
            System.err.println(MiscUtil.getException(e));
            Assert.fail();
        }

    }
    
    @AfterClass
    public static void tearDownClass() {
        poPaymentRequest = null;
        poApp = null;
    }
}
