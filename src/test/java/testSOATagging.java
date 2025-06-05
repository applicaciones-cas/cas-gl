
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.cas.gl.SOATagging;
import org.guanzon.cas.gl.services.GLControllers;
import org.guanzon.cas.gl.services.SOATaggingControllers;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Arsiela 03-12-2025
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class testSOATagging {

    static GRiderCAS instance;
    static SOATagging poSOATaggingController;

    @BeforeClass
    public static void setUpClass() {
        System.setProperty("sys.default.path.metadata", "D:/GGC_Maven_Systems/config/metadata/new/");

        instance = MiscUtil.Connect();

        poSOATaggingController = new GLControllers(instance, null).SOATagging();
    }

    @Test
    public void testNewTransaction() {
        String branchCd = instance.getBranchCode();
        String industryId = "01";
        String remarks = "this is a test Class 4.";

        String stockId = "C0W125000001";
        int quantity = 110;

        JSONObject loJSON;

        try {

            loJSON = poSOATaggingController.InitTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            loJSON = poSOATaggingController.NewTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            try {
                poSOATaggingController.setIndustryId(industryId);
                poSOATaggingController.setCompanyId("0002");
                poSOATaggingController.setCategoryId("0001");

                poSOATaggingController.initFields();
                poSOATaggingController.Master().setBranchCode(branchCd); 
                Assert.assertEquals(poSOATaggingController.Master().getBranchCode(), branchCd);
//                poSOATaggingController.Master().setIndustryCode(industryId); 
//                Assert.assertEquals(poSOATaggingController.Master().getIndustryCode(), industryId);
//                poSOATaggingController.Master().setTransactionDate(instance.getServerDate()); 
//                Assert.assertEquals(poSOATaggingController.Master().getTransactionDate(), instance.getServerDate());
                poSOATaggingController.Master().setClientId("C00124000020"); 
                Assert.assertEquals(poSOATaggingController.Master().getClientId(), "C00124000020");

                poSOATaggingController.Master().setRemarks(remarks);
                Assert.assertEquals(poSOATaggingController.Master().getRemarks(), remarks);

                poSOATaggingController.Detail(0).setSourceNo("test");
                poSOATaggingController.Detail(0).setSourceCode("");
                poSOATaggingController.Detail(0).setDebitAmount(0.0);
                poSOATaggingController.Detail(0).setCreditAmount(0.0);
                poSOATaggingController.Detail(0).setAppliedAmount(0.0);
//                poSOATaggingController.Detail(0).setVATAmt(0.0);
//                poSOATaggingController.Detail(0).setNonVATSale(0.0);
//                poSOATaggingController.Detail(0).setVATExempt(0.0);
//                poSOATaggingController.Detail(0).setZeroVATSale(0.0);
//                poSOATaggingController.Detail(0).setTaxAmount(0.0);
                poSOATaggingController.AddDetail();

//                poSOATaggingController.Detail(1).setStockId("C0W125000004");
//                poSOATaggingController.Detail(1).setQuantity(quantity);
//                poSOATaggingController.Detail(1).isSerialized(true);
//                poSOATaggingController.Detail(1).setQuantity(0);
//                poSOATaggingController.AddDetail();
//
//                poSOATaggingController.Detail(2).setStockId("M00125000001");
//                poSOATaggingController.Detail(2).setQuantity(quantity);
//                poSOATaggingController.Detail(2).isSerialized(true);
//                poSOATaggingController.Detail(2).setQuantity(0);
//                poSOATaggingController.AddDetail();
//
//                poSOATaggingController.Detail(3).setStockId("M00124000002");
//                poSOATaggingController.Detail(3).setQuantity(quantity);
//                poSOATaggingController.Detail(3).isSerialized(true);
//                poSOATaggingController.Detail(3).setQuantity(1);
//                poSOATaggingController.AddDetail();

                poSOATaggingController.computeFields();

                //populate POR Serial
//                loJSON = poSOATaggingController.getPurchaseOrderReceivingSerial(1);
//                if("success".equals((String) loJSON.get("result"))){
//                    System.out.println("inv serial cnt : " + poSOATaggingController.getPurchaseOrderReceivingSerialCount());
//                    poSOATaggingController.PurchaseOrderReceivingSerialList(0).setLocationId("333");
//                    poSOATaggingController.PurchaseOrderReceivingSerialList(0).setStockId(stockId);
//                    poSOATaggingController.PurchaseOrderReceivingSerialList(0).setSerial01("mobilephone101");
//                    poSOATaggingController.PurchaseOrderReceivingSerialList(0).setSerial02("mobilephone202");
////                    poSOATaggingController.PurchaseOrderReceivingSerialList(0).setPlateNo("001sa1");
////                    poSOATaggingController.PurchaseOrderReceivingSerialList(0).setConductionStickerNo("333");
//                }
                //populate POR Serial
//                loJSON = poSOATaggingController.getPurchaseOrderReceivingSerial(4);
//                if("success".equals((String) loJSON.get("result"))){
//                    System.out.println("inv serial cnt : " + poSOATaggingController.getPurchaseOrderReceivingSerialCount());
//                    poSOATaggingController.PurchaseOrderReceivingSerialList(1).setLocationId("333");
//                    poSOATaggingController.PurchaseOrderReceivingSerialList(1).setStockId("M00124000002");
//                    poSOATaggingController.PurchaseOrderReceivingSerialList(1).setSerial01("mob");
//                    poSOATaggingController.PurchaseOrderReceivingSerialList(1).setSerial02("phone");
////                    poSOATaggingController.PurchaseOrderReceivingSerialList(0).setPlateNo("001sa1");
////                    poSOATaggingController.PurchaseOrderReceivingSerialList(0).setConductionStickerNo("333");
//                }
                System.out.println("Industry ID : " + instance.getIndustry());
                System.out.println("Industry : " + poSOATaggingController.Master().Industry().getDescription());
                System.out.println("TransNox : " + poSOATaggingController.Master().getTransactionNo());

                loJSON = poSOATaggingController.SaveTransaction();
                if (!"success".equals((String) loJSON.get("result"))) {
                    System.err.println((String) loJSON.get("message"));
                    Assert.fail();
                }

            } catch (SQLException | GuanzonException ex) {
                Logger.getLogger(testSOATagging.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (ExceptionInInitializerError e) {
            System.err.println(MiscUtil.getException(e));
            Assert.fail();
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(testSOATagging.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

//    @Test
    public void testUpdateTransaction() {
        JSONObject loJSON;

        try {
            loJSON = poSOATaggingController.InitTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            loJSON = poSOATaggingController.OpenTransaction("P0w125000001");
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            loJSON = poSOATaggingController.UpdateTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            
            
            poSOATaggingController.Master().setCompanyId("0001"); 
            Assert.assertEquals(poSOATaggingController.Master().getCompanyId(), "0001");
            poSOATaggingController.Master().setSOANumber("soano"); 
            Assert.assertEquals(poSOATaggingController.Master().getSOANumber(), "soano");
            
            //Populate purhcase receiving serials
//            for(int lnCtr = 0; lnCtr <= poSOATaggingController.getDetailCount()-1; lnCtr++){
//                poSOATaggingController.getPurchaseOrderReceivingSerial(poSOATaggingController.Detail(lnCtr).getEntryNo());
//            }
//            poSOATaggingController.Detail(1).setQuantity(0);
//            poSOATaggingController.AddDetail();
//            for(int lnCtr = 0;lnCtr <= poSOATaggingController.getDetailCount()-1; lnCtr++){
//                poSOATaggingController.Detail(0).setQuantity(2);
//                poSOATaggingController.Detail(1).setQuantity(5);
//                System.out.println("DATA Before SAVE TRANSACTION Method");
//                System.out.println("TransNo : " + (lnCtr+1) + " : " + poSOATaggingController.Detail(lnCtr).getTransactionNo());
//                System.out.println("OrderNo : " + (lnCtr+1) + " : " + poSOATaggingController.Detail(lnCtr).getOrderNo());
//                System.out.println("StockId : " + (lnCtr+1) + " : " + poSOATaggingController.Detail(lnCtr).getStockId());
//                System.out.println("Quantty : " + (lnCtr+1) + " : " + poSOATaggingController.Detail(lnCtr).getQuantity());
//                System.out.println("---------------------------------------------------------------------");
//            }
            loJSON = poSOATaggingController.SaveTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
        } catch (CloneNotSupportedException | SQLException e) {
            System.err.println(MiscUtil.getException(e));
            Assert.fail();
        } catch (GuanzonException ex) {
            Logger.getLogger(testSOATagging.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }

    }
//
////    @Test
//    public void testgetCachePayablesList() {
//        String industryId = "02";
//        String companyId = "0001";
//        String supplierId = "C00124000009";
//
//        JSONObject loJSON;
//
//        loJSON = poSOATaggingController.InitTransaction();
//        if (!"success".equals((String) loJSON.get("result"))) {
//            System.err.println((String) loJSON.get("message"));
//            Assert.fail();
//        }
//
////        poSOATaggingController.Master().setIndustryId(industryId); 
////        poSOATaggingController.Master().setCompanyId(companyId); 
////        poSOATaggingController.Master().setSupplierId(supplierId); 
//
////        loJSON = poSOATaggingController.getApprovedPurchaseOrder();
////        if (!"success".equals((String) loJSON.get("result"))) {
////            System.err.println((String) loJSON.get("message"));
////            Assert.fail();
////        }
//
//        //retreiving using column index
//        for (int lnCtr = 0; lnCtr <= poSOATaggingController.getCachePayablesCount() - 1; lnCtr++) {
//            System.out.println("PO Row No ->> " + lnCtr);
////            System.out.println("PO Transaction No ->> " + poSOATaggingController.CachePayableList(lnCtr).getTransactionNo());
////            System.out.println("PO Transaction Date ->> " + poSOATaggingController.CachePayableList(lnCtr).getTransactionDate());
////            System.out.println("PO Industry ->> " + poSOATaggingController.CachePayableList(lnCtr).Industry().getDescription());
////            System.out.println("PO Company ->> " + poSOATaggingController.CachePayableList(lnCtr).Company().getCompanyName());
////            System.out.println("PO Supplier ->> " + poSOATaggingController.CachePayableList(lnCtr).Supplier().getCompanyName());
//            System.out.println("----------------------------------------------------------------------------------");
//        }
//    }
//
////     @Test
//    public void testOpenTransaction() {
//        JSONObject loJSON;
//
//        try {
//            loJSON = poSOATaggingController.InitTransaction();
//            if (!"success".equals((String) loJSON.get("result"))) {
//                System.err.println((String) loJSON.get("message"));
//                Assert.fail();
//            }
//
//            loJSON = poSOATaggingController.OpenTransaction("M00125000002");
//            if (!"success".equals((String) loJSON.get("result"))) {
//                System.err.println((String) loJSON.get("message"));
//                Assert.fail();
//            }
//
//            //retreiving using column index
//            for (int lnCol = 1; lnCol <= poSOATaggingController.Master().getColumnCount(); lnCol++) {
//                System.out.println(poSOATaggingController.Master().getColumn(lnCol) + " ->> " + poSOATaggingController.Master().getValue(lnCol));
//            }
//            //retreiving using field descriptions
//            System.out.println(poSOATaggingController.Master().Branch().getBranchName());
//            System.out.println(poSOATaggingController.Master().Category().getDescription());
//
//            //retreiving using column index
//            for (int lnCtr = 0; lnCtr <= poSOATaggingController.Detail().size() - 1; lnCtr++) {
//                for (int lnCol = 1; lnCol <= poSOATaggingController.Detail(lnCtr).getColumnCount(); lnCol++) {
//                    System.out.println(poSOATaggingController.Detail(lnCtr).getColumn(lnCol) + " ->> " + poSOATaggingController.Detail(lnCtr).getValue(lnCol));
//                }
//            }
//        } catch (CloneNotSupportedException e) {
//            System.err.println(MiscUtil.getException(e));
//            Assert.fail();
//        } catch (SQLException | GuanzonException ex) {
//            Logger.getLogger(testSOATagging.class.getName()).log(Level.SEVERE, null, ex);
//        }
//
//    }
//
//    @Test
    public void testConfirmTransaction() {
        JSONObject loJSON;

        try {
            loJSON = poSOATaggingController.InitTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            loJSON = poSOATaggingController.OpenTransaction("P0w125000001");
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            //retreiving using column index
            for (int lnCol = 1; lnCol <= poSOATaggingController.Master().getColumnCount(); lnCol++) {
                System.out.println(poSOATaggingController.Master().getColumn(lnCol) + " ->> " + poSOATaggingController.Master().getValue(lnCol));
            }
            //retreiving using field descriptions
            System.out.println(poSOATaggingController.Master().Branch().getBranchName());
            System.out.println(poSOATaggingController.Master().Category().getDescription());

            //retreiving using column index
            for (int lnCtr = 0; lnCtr <= poSOATaggingController.Detail().size() - 1; lnCtr++) {
                for (int lnCol = 1; lnCol <= poSOATaggingController.Detail(lnCtr).getColumnCount(); lnCol++) {
                    System.out.println(poSOATaggingController.Detail(lnCtr).getColumn(lnCol) + " ->> " + poSOATaggingController.Detail(lnCtr).getValue(lnCol));
                }
            }

            loJSON = poSOATaggingController.ConfirmTransaction("test confirm");
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            System.out.println((String) loJSON.get("message"));
        } catch (CloneNotSupportedException | ParseException e) {
            System.err.println(MiscUtil.getException(e));
            Assert.fail();
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(testSOATagging.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
//
////    @Test
//    public void testReturnTransaction() {
//        JSONObject loJSON;
//
//        try {
//            loJSON = poSOATaggingController.InitTransaction();
//            if (!"success".equals((String) loJSON.get("result"))) {
//                System.err.println((String) loJSON.get("message"));
//                Assert.fail();
//            }
//
//            loJSON = poSOATaggingController.OpenTransaction("M00125000003");
//            if (!"success".equals((String) loJSON.get("result"))) {
//                System.err.println((String) loJSON.get("message"));
//                Assert.fail();
//            }
//
//            //retreiving using column index
//            for (int lnCol = 1; lnCol <= poSOATaggingController.Master().getColumnCount(); lnCol++) {
//                System.out.println(poSOATaggingController.Master().getColumn(lnCol) + " ->> " + poSOATaggingController.Master().getValue(lnCol));
//            }
//            //retreiving using field descriptions
//            System.out.println(poSOATaggingController.Master().Branch().getBranchName());
//            System.out.println(poSOATaggingController.Master().Category().getDescription());
//
//            //retreiving using column index
//            for (int lnCtr = 0; lnCtr <= poSOATaggingController.Detail().size() - 1; lnCtr++) {
//                for (int lnCol = 1; lnCol <= poSOATaggingController.Detail(lnCtr).getColumnCount(); lnCol++) {
//                    System.out.println(poSOATaggingController.Detail(lnCtr).getColumn(lnCol) + " ->> " + poSOATaggingController.Detail(lnCtr).getValue(lnCol));
//                }
//            }
//
////            loJSON = poSOATaggingController.ReturnTransaction("test return");
////            if (!"success".equals((String) loJSON.get("result"))){
////                System.err.println((String) loJSON.get("message"));
////                Assert.fail();
////            } 
//            System.out.println((String) loJSON.get("message"));
//        } catch (CloneNotSupportedException e) {
//            System.err.println(MiscUtil.getException(e));
//            Assert.fail();
//        } catch (SQLException | GuanzonException ex) {
//            Logger.getLogger(testSOATagging.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }

//    @Test
//    public void testApproveTransaction() {
//        JSONObject loJSON;
//        
//        try {
//            loJSON = poSOATaggingController.InitTransaction();
//            if (!"success".equals((String) loJSON.get("result"))){
//                System.err.println((String) loJSON.get("message"));
//                Assert.fail();
//            } 
//
//            loJSON = poSOATaggingController.OpenTransaction("M00125000016");
//            if (!"success".equals((String) loJSON.get("result"))){
//                System.err.println((String) loJSON.get("message"));
//                Assert.fail();
//            } 
//
//            //retreiving using column index
//            for (int lnCol = 1; lnCol <= poSOATaggingController.Master().getColumnCount(); lnCol++){
//                System.out.println(poSOATaggingController.Master().getColumn(lnCol) + " ->> " + poSOATaggingController.Master().getValue(lnCol));
//            }
//            //retreiving using field descriptions
//            System.out.println(poSOATaggingController.Master().Branch().getBranchName());
//            System.out.println(poSOATaggingController.Master().Category().getDescription());
//
//            //retreiving using column index
//            for (int lnCtr = 0; lnCtr <= poSOATaggingController.Detail().size() - 1; lnCtr++){
//                for (int lnCol = 1; lnCol <= poSOATaggingController.Detail(lnCtr).getColumnCount(); lnCol++){
//                    System.out.println(poSOATaggingController.Detail(lnCtr).getColumn(lnCol) + " ->> " + poSOATaggingController.Detail(lnCtr).getValue(lnCol));
//                }
//            }
//            
//            loJSON = poSOATaggingController.ApproveTransaction("test approve");
//            if (!"success".equals((String) loJSON.get("result"))){
//                System.err.println((String) loJSON.get("message"));
//                Assert.fail();
//            } 
//            
//            System.out.println((String) loJSON.get("message"));
//        } catch (CloneNotSupportedException |ParseException e) {
//            System.err.println(MiscUtil.getException(e));
//            Assert.fail();
//        } catch (SQLException | GuanzonException ex) {
//            Logger.getLogger(testSOATagging.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
}
