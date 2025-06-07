
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.cas.gl.Disbursement;
import org.guanzon.cas.gl.services.GLControllers;
import org.json.simple.JSONObject;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class testAddtoDisbursementDetail {

    static GRiderCAS poApp;
    static Disbursement poDisbursement;

    @BeforeClass
    public static void setUpClass() {
        try {
            System.setProperty("sys.default.path.metadata", "D:/GGC_Maven_Systems/config/metadata/new/");
            
            poApp = MiscUtil.Connect();
            poDisbursement = new GLControllers(poApp, null).Disbursement();
        } catch (SQLException ex) {
            Logger.getLogger(testPRFOpenIsuance.class.getName()).log(Level.SEVERE, null, ex);
        } catch (GuanzonException ex) {
            Logger.getLogger(testPRFOpenIsuance.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    @Test
    public void testOpenTransaction() {
        JSONObject loJSON;
        String transactionNo = "GCC225000007";
        String TransactionType = "PRF";

        try {
           loJSON = poDisbursement.addUnifiedPaymentToDisbursement(transactionNo,TransactionType);
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
//            loJSON = poRecurringIssuance.openRecord(particularNo, Branch, payeeID, AcctNo);
//            if (!"success".equals((String) loJSON.get("result"))) {
//                System.err.println((String) loJSON.get("message"));
//                Assert.fail();
//            }
        } catch (SQLException | GuanzonException | CloneNotSupportedException e) {
            Logger.getLogger(MiscUtil.getException(e));
            System.err.println(MiscUtil.getException(e));
            Assert.fail();
        } 

    }
    @AfterClass
    public static void tearDownClass() {
        poDisbursement = null;
        poApp = null;
    }
}

