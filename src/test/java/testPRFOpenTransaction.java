
import java.sql.SQLException;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.cas.gl.services.GLControllers;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class testPRFOpenTransaction {

    
    static GRiderCAS poApp;
    static GLControllers poPaymentRequest;

    @BeforeClass
    public static void setUpClass() {
        System.setProperty("sys.default.path.metadata", "D:/GGC_Maven_Systems/config/metadata/new/");

        poApp = MiscUtil.Connect();

        poPaymentRequest = new GLControllers(poApp, null);
    }
    

    @Test
    public void testOpenTransaction() {
        JSONObject loJSON;
        try {
            loJSON = poPaymentRequest.PaymentRequest().InitTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            loJSON = poPaymentRequest.PaymentRequest().OpenTransaction("GCC225000001");
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            poApp.getDepartment();


            System.out.println("Transaction No: " + poPaymentRequest.PaymentRequest().Master().getTransactionNo());
            System.out.println("Transaction Date : " + poPaymentRequest.PaymentRequest().Master().getTransactionDate().toString());
            System.out.println("Branch: " + poPaymentRequest.PaymentRequest().Master().Branch().getBranchName());
            System.out.println("Department: " + poPaymentRequest.PaymentRequest().Master().Department().getDescription());
//            System.out.println("Payee: " + poPaymentRequest.PaymentRequest().Master().getPayeeID());
            System.out.println("Series No: " + poPaymentRequest.PaymentRequest().Master().getSeriesNo());
            System.out.println("Entry No: " + poPaymentRequest.PaymentRequest().Master().getEntryNo());
            System.out.println("");
            int detailSize = poPaymentRequest.PaymentRequest().Detail().size();
            if (detailSize > 0) {
                 for (int lnCtr = 0; lnCtr < poPaymentRequest.PaymentRequest().Detail().size(); lnCtr++) {
                    System.out.println("DETAIL------------------- " + (lnCtr + 1));
                    System.out.println("TRANSACTION NO : " + poPaymentRequest.PaymentRequest().Master().getTransactionNo());
                    System.out.println("ENTRY No: " + poPaymentRequest.PaymentRequest().Detail(lnCtr).getEntryNo());
                    System.out.println("PARTICULAR ID : " + poPaymentRequest.PaymentRequest().Detail(lnCtr).getParticularID());
                    System.out.println("");
                 }
            }
        } catch (CloneNotSupportedException | SQLException | GuanzonException e) {
            System.err.println(MiscUtil.getException(e));
            Assert.fail();
        }

    }

//    @Test
//    public void testUpdateTransaction() throws GuanzonException {
//        JSONObject loJSON;
//
//        try {
//            loJSON = (JSONObject) poPaymentRequest.PaymentRequest().InitTransaction();
//            if (!"success".equals((String) loJSON.get("result"))) {
//                System.err.println((String) loJSON.get("message"));
//                Assert.fail();
//            }
//
//            loJSON = (JSONObject) poPaymentRequest.PaymentRequest().OpenTransaction("A00125000001");
//            if (!"success".equals((String) loJSON.get("result"))) {
//                System.err.println((String) loJSON.get("message"));
//                Assert.fail();
//            }
//
//            loJSON = (JSONObject) poPaymentRequest.PaymentRequest().UpdateTransaction();
//            if (!"success".equals((String) loJSON.get("result"))) {
//                System.err.println((String) loJSON.get("message"));
//                Assert.fail();
//            }
//
//            poPaymentRequest.PaymentRequest().Detail(0).setQuantityOnHand(12);
//            poPaymentRequest.PaymentRequest().Detail(0).setModifiedDate(poApp.getServerDate());
//            poPaymentRequest.PaymentRequest().Detail(1).setQuantityOnHand(22);
//            poPaymentRequest.PaymentRequest().Detail(1).setModifiedDate(poApp.getServerDate());
//
//            loJSON = poPaymentRequest.PaymentRequest().SaveTransaction();
//            if (!"success".equals((String) loJSON.get("result"))) {
//                System.err.println((String) loJSON.get("message"));
//                Assert.fail();
//            }
//        } catch (CloneNotSupportedException | SQLException e) {
//            System.err.println(MiscUtil.getException(e));
//            Assert.fail();
//        }
//
//    }
//
//    @Test
//    public void testConfirmTransaction() {
//        JSONObject loJSON;
//
//        try {
//            loJSON = poPaymentRequest.PaymentRequest().InitTransaction();
//            if (!"success".equals((String) loJSON.get("result"))) {
//                System.err.println((String) loJSON.get("message"));
//                Assert.fail();
//            }
//
//            loJSON = poPaymentRequest.PaymentRequest().OpenTransaction("M00125000004");
//            if (!"success".equals((String) loJSON.get("result"))) {
//                System.err.println((String) loJSON.get("message"));
//                Assert.fail();
//            }
//
//            System.out.println("Transaction No: " + poPaymentRequest.PaymentRequest().Master().getTransactionNo());
//            System.out.println("Industry : " + poPaymentRequest.PaymentRequest().Master().Industry().getDescription());
//            System.out.println("Company: " + poPaymentRequest.PaymentRequest().Master().Company().getCompanyName());
//            System.out.println("Supplier: " + poPaymentRequest.PaymentRequest().Master().Supplier().getCompanyName());
//            System.out.println("Destination: " + poPaymentRequest.PaymentRequest().Master().Branch().getBranchName());
//            System.out.println("ReferNo: " + poPaymentRequest.PaymentRequest().Master().getReference());
//            System.out.println("Term: " + poPaymentRequest.PaymentRequest().Master().Term().getTermValue());
//
//            System.out.println("==== DETAIL TABLE ====");
//            int detailSize = poPaymentRequest.PaymentRequest().Detail().size();
//            if (detailSize > 0) {
//                // Print column headers using the first detail row
//                for (int lnCol = 1; lnCol <= poPaymentRequest.PaymentRequest().Detail(0).getColumnCount(); lnCol++) {
//                    System.out.printf("%-20s", poPaymentRequest.PaymentRequest().Detail(0).getColumn(lnCol));
//                }
//                System.out.println();
//
//                // Print detail row data
//                for (int lnCtr = 0; lnCtr < detailSize; lnCtr++) {
//                    for (int lnCol = 1; lnCol <= poPaymentRequest.PaymentRequest().Detail(lnCtr).getColumnCount(); lnCol++) {
//                        System.out.printf("%-20s", poPaymentRequest.PaymentRequest().Detail(lnCtr).getValue(lnCol));
//                    }
//                    System.out.println();
//                }
//            } else {
//                System.out.println("No detail rows found.");
//            }
//
//            loJSON = poPaymentRequest.PaymentRequest().ConfirmTransaction("");
//            if (!"success".equals((String) loJSON.get("result"))) {
//                System.err.println((String) loJSON.get("message"));
//                Assert.fail();
//            }
//
//            System.out.println((String) loJSON.get("message"));
//        } catch (CloneNotSupportedException | ParseException | SQLException | GuanzonException e) {
//            System.err.println(MiscUtil.getException(e));
//            Assert.fail();
//        }
//
//    }
//
//    @Test
//    public void testApproveTransaction() {
//        JSONObject loJSON;
//
//        try {
//            loJSON = poPaymentRequest.PaymentRequest().InitTransaction();
//            if (!"success".equals((String) loJSON.get("result"))) {
//                System.err.println((String) loJSON.get("message"));
//                Assert.fail();
//            }
//
//            loJSON = poPaymentRequest.PaymentRequest().OpenTransaction("M00125000004");
//            if (!"success".equals((String) loJSON.get("result"))) {
//                System.err.println((String) loJSON.get("message"));
//                Assert.fail();
//            }
//
//            System.out.println("Transaction No: " + poPaymentRequest.PaymentRequest().Master().getTransactionNo());
//            System.out.println("Industry : " + poPaymentRequest.PaymentRequest().Master().Industry().getDescription());
//            System.out.println("Company: " + poPaymentRequest.PaymentRequest().Master().Company().getCompanyName());
//            System.out.println("Supplier: " + poPaymentRequest.PaymentRequest().Master().Supplier().getCompanyName());
//            System.out.println("Destination: " + poPaymentRequest.PaymentRequest().Master().Branch().getBranchName());
//            System.out.println("ReferNo: " + poPaymentRequest.PaymentRequest().Master().getReference());
//            System.out.println("Term: " + poPaymentRequest.PaymentRequest().Master().Term().getTermValue());
//
//            System.out.println("==== DETAIL TABLE ====");
//            int detailSize = poPaymentRequest.PaymentRequest().Detail().size();
//            if (detailSize > 0) {
//                // Print column headers using the first detail row
//                for (int lnCol = 1; lnCol <= poPaymentRequest.PaymentRequest().Detail(0).getColumnCount(); lnCol++) {
//                    System.out.printf("%-20s", poPaymentRequest.PaymentRequest().Detail(0).getColumn(lnCol));
//                }
//                System.out.println();
//
//                // Print detail row data
//                for (int lnCtr = 0; lnCtr < detailSize; lnCtr++) {
//                    for (int lnCol = 1; lnCol <= poPaymentRequest.PaymentRequest().Detail(lnCtr).getColumnCount(); lnCol++) {
//                        System.out.printf("%-20s", poPaymentRequest.PaymentRequest().Detail(lnCtr).getValue(lnCol));
//                    }
//                    System.out.println();
//                }
//            } else {
//                System.out.println("No detail rows found.");
//            }
//
//            loJSON = poPaymentRequest.PaymentRequest().ApproveTransaction("Approved Test");
//            if (!"success".equals((String) loJSON.get("result"))) {
//                System.err.println((String) loJSON.get("message"));
//                Assert.fail();
//            }
//
//            System.out.println((String) loJSON.get("message"));
//        } catch (CloneNotSupportedException | ParseException | SQLException | GuanzonException e) {
//            System.err.println(MiscUtil.getException(e));
//            Assert.fail();
//        }
//
//    }
//
//    @Test
//    public void testReturnTransaction() {
//        JSONObject loJSON;
//
//        try {
//            loJSON = poPaymentRequest.PaymentRequest().InitTransaction();
//            if (!"success".equals((String) loJSON.get("result"))) {
//                System.err.println((String) loJSON.get("message"));
//                Assert.fail();
//            }
//
//            loJSON = poPaymentRequest.PaymentRequest().OpenTransaction("M00125000004");
//            if (!"success".equals((String) loJSON.get("result"))) {
//                System.err.println((String) loJSON.get("message"));
//                Assert.fail();
//            }
//
//            System.out.println("Transaction No: " + poPaymentRequest.PaymentRequest().Master().getTransactionNo());
//            System.out.println("Industry : " + poPaymentRequest.PaymentRequest().Master().Industry().getDescription());
//            System.out.println("Company: " + poPaymentRequest.PaymentRequest().Master().Company().getCompanyName());
//            System.out.println("Supplier: " + poPaymentRequest.PaymentRequest().Master().Supplier().getCompanyName());
//            System.out.println("Destination: " + poPaymentRequest.PaymentRequest().Master().Branch().getBranchName());
//            System.out.println("ReferNo: " + poPaymentRequest.PaymentRequest().Master().getReference());
//            System.out.println("Term: " + poPaymentRequest.PaymentRequest().Master().Term().getTermValue());
//
//            System.out.println("==== DETAIL TABLE ====");
//            int detailSize = poPaymentRequest.PaymentRequest().Detail().size();
//            if (detailSize > 0) {
//                // Print column headers using the first detail row
//                for (int lnCol = 1; lnCol <= poPaymentRequest.PaymentRequest().Detail(0).getColumnCount(); lnCol++) {
//                    System.out.printf("%-20s", poPaymentRequest.PaymentRequest().Detail(0).getColumn(lnCol));
//                }
//                System.out.println();
//
//                // Print detail row data
//                for (int lnCtr = 0; lnCtr < detailSize; lnCtr++) {
//                    for (int lnCol = 1; lnCol <= poPaymentRequest.PaymentRequest().Detail(lnCtr).getColumnCount(); lnCol++) {
//                        System.out.printf("%-20s", poPaymentRequest.PaymentRequest().Detail(lnCtr).getValue(lnCol));
//                    }
//                    System.out.println();
//                }
//            } else {
//                System.out.println("No detail rows found.");
//            }
//
//            loJSON = poPaymentRequest.PaymentRequest().ReturnTransaction("Returned Test");
//
//            if (!"success".equals((String) loJSON.get("result"))) {
//                System.err.println((String) loJSON.get("message"));
//                Assert.fail();
//            }
//
//            System.out.println((String) loJSON.get("message"));
//        } catch (CloneNotSupportedException | ParseException | SQLException | GuanzonException e) {
//            System.err.println(MiscUtil.getException(e));
//            Assert.fail();
//        }
//
//    }
//
//    @Test
//    public void testVoidTransaction() {
//        JSONObject loJSON;
//
//        try {
//            loJSON = poPaymentRequest.PaymentRequest().InitTransaction();
//            if (!"success".equals((String) loJSON.get("result"))) {
//                System.err.println((String) loJSON.get("message"));
//                Assert.fail();
//            }
//
//            loJSON = poPaymentRequest.PaymentRequest().OpenTransaction("M00125000004");
//            if (!"success".equals((String) loJSON.get("result"))) {
//                System.err.println((String) loJSON.get("message"));
//                Assert.fail();
//            }
//
//            System.out.println("Transaction No: " + poPaymentRequest.PaymentRequest().Master().getTransactionNo());
//            System.out.println("Industry : " + poPaymentRequest.PaymentRequest().Master().Industry().getDescription());
//            System.out.println("Company: " + poPaymentRequest.PaymentRequest().Master().Company().getCompanyName());
//            System.out.println("Supplier: " + poPaymentRequest.PaymentRequest().Master().Supplier().getCompanyName());
//            System.out.println("Destination: " + poPaymentRequest.PaymentRequest().Master().Branch().getBranchName());
//            System.out.println("ReferNo: " + poPaymentRequest.PaymentRequest().Master().getReference());
//            System.out.println("Term: " + poPaymentRequest.PaymentRequest().Master().Term().getTermValue());
//
//            System.out.println("==== DETAIL TABLE ====");
//            int detailSize = poPaymentRequest.PaymentRequest().Detail().size();
//            if (detailSize > 0) {
//                // Print column headers using the first detail row
//                for (int lnCol = 1; lnCol <= poPaymentRequest.PaymentRequest().Detail(0).getColumnCount(); lnCol++) {
//                    System.out.printf("%-20s", poPaymentRequest.PaymentRequest().Detail(0).getColumn(lnCol));
//                }
//                System.out.println();
//
//                // Print detail row data
//                for (int lnCtr = 0; lnCtr < detailSize; lnCtr++) {
//                    for (int lnCol = 1; lnCol <= poPaymentRequest.PaymentRequest().Detail(lnCtr).getColumnCount(); lnCol++) {
//                        System.out.printf("%-20s", poPaymentRequest.PaymentRequest().Detail(lnCtr).getValue(lnCol));
//                    }
//                    System.out.println();
//                }
//            } else {
//                System.out.println("No detail rows found.");
//            }
//
//            loJSON = poPaymentRequest.PaymentRequest().VoidTransaction("Void Test");
//
//            if (!"success".equals((String) loJSON.get("result"))) {
//                System.err.println((String) loJSON.get("message"));
//                Assert.fail();
//            }
//
//            System.out.println((String) loJSON.get("message"));
//        } catch (CloneNotSupportedException | ParseException | SQLException | GuanzonException e) {
//            System.err.println(MiscUtil.getException(e));
//            Assert.fail();
//        }
//    }
//
//    @Test
//    public void testPostTransaction() {
//        JSONObject loJSON;
//
//        try {
//            loJSON = poPaymentRequest.PaymentRequest().InitTransaction();
//            if (!"success".equals((String) loJSON.get("result"))) {
//                System.err.println((String) loJSON.get("message"));
//                Assert.fail();
//            }
//
//            loJSON = poPaymentRequest.PaymentRequest().OpenTransaction("M00125000004");
//            if (!"success".equals((String) loJSON.get("result"))) {
//                System.err.println((String) loJSON.get("message"));
//                Assert.fail();
//            }
//
//            System.out.println("Transaction No: " + poPaymentRequest.PaymentRequest().Master().getTransactionNo());
//            System.out.println("Industry : " + poPaymentRequest.PaymentRequest().Master().Industry().getDescription());
//            System.out.println("Company: " + poPaymentRequest.PaymentRequest().Master().Company().getCompanyName());
//            System.out.println("Supplier: " + poPaymentRequest.PaymentRequest().Master().Supplier().getCompanyName());
//            System.out.println("Destination: " + poPaymentRequest.PaymentRequest().Master().Branch().getBranchName());
//            System.out.println("ReferNo: " + poPaymentRequest.PaymentRequest().Master().getReference());
//            System.out.println("Term: " + poPaymentRequest.PaymentRequest().Master().Term().getTermValue());
//
//            System.out.println("==== DETAIL TABLE ====");
//            int detailSize = poPaymentRequest.PaymentRequest().Detail().size();
//            if (detailSize > 0) {
//                // Print column headers using the first detail row
//                for (int lnCol = 1; lnCol <= poPaymentRequest.PaymentRequest().Detail(0).getColumnCount(); lnCol++) {
//                    System.out.printf("%-20s", poPaymentRequest.PaymentRequest().Detail(0).getColumn(lnCol));
//                }
//                System.out.println();
//
//                // Print detail row data
//                for (int lnCtr = 0; lnCtr < detailSize; lnCtr++) {
//                    for (int lnCol = 1; lnCol <= poPaymentRequest.PaymentRequest().Detail(lnCtr).getColumnCount(); lnCol++) {
//                        System.out.printf("%-20s", poPaymentRequest.PaymentRequest().Detail(lnCtr).getValue(lnCol));
//                    }
//                    System.out.println();
//                }
//            } else {
//                System.out.println("No detail rows found.");
//            }
//
//            loJSON = poPaymentRequest.PaymentRequest().PostTransaction("Post Test");
//            if (!"success".equals((String) loJSON.get("result"))) {
//                System.err.println((String) loJSON.get("message"));
//                Assert.fail();
//            }
//
//            System.out.println((String) loJSON.get("message"));
//        } catch (CloneNotSupportedException | ParseException | SQLException | GuanzonException e) {
//            System.err.println(MiscUtil.getException(e));
//            Assert.fail();
//        }
//    }

    @AfterClass
    public static void tearDownClass() {
        poPaymentRequest = null;
        poApp = null;
    }
}
