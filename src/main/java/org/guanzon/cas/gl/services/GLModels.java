package org.guanzon.cas.gl.services;

import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.cas.gl.model.Model_Account_Chart;
import org.guanzon.cas.gl.model.Model_Journal_Detail;
import org.guanzon.cas.gl.model.Model_Journal_Master;
import org.guanzon.cas.gl.model.Model_Particular;
import org.guanzon.cas.gl.model.Model_Payee;
import org.guanzon.cas.gl.model.Model_Payment_Request_Detail;
import org.guanzon.cas.gl.model.Model_Payment_Request_Master;
import org.guanzon.cas.gl.model.Model_Recurring_Issuance;
import org.guanzon.cas.gl.model.Model_Transaction_Account_Chart;

public class GLModels {
    public GLModels(GRiderCAS applicationDriver){
        poGRider = applicationDriver;
    }
    
    public Model_Account_Chart Account_Chart(){
        if (poGRider == null){
            System.err.println("GLModels.Account_Chart: Application driver is not set.");
            return null;
        }
        
        if (poAccountChart == null){
            poAccountChart = new Model_Account_Chart();
            poAccountChart.setApplicationDriver(poGRider);
            poAccountChart.setXML("Model_Account_Chart");
            poAccountChart.setTableName("Account_Chart");
            poAccountChart.initialize();
        }

        return poAccountChart;
    }
    
    public Model_Transaction_Account_Chart Transaction_Account_Chart(){
        if (poGRider == null){
            System.err.println("GLModels.Account_Chart: Application driver is not set.");
            return null;
        }
        
        if (poGeneralLedger == null){
            poGeneralLedger = new Model_Transaction_Account_Chart();
            poGeneralLedger.setApplicationDriver(poGRider);
            poGeneralLedger.setXML("Model_Transaction_Account_Chart");
            poGeneralLedger.setTableName("Transaction_Account_Chart");
            poGeneralLedger.initialize();
        }

        return poGeneralLedger;
    }
    
    public Model_Journal_Master Journal_Master(){
        if (poGRider == null){
            System.err.println("GLModels.Journal_Master: Application driver is not set.");
            return null;
        }
        
        if (poJournalMaster == null){
            poJournalMaster = new Model_Journal_Master();
            poJournalMaster.setApplicationDriver(poGRider);
            poJournalMaster.setXML("Model_Journal_Master");
            poJournalMaster.setTableName("Journal_Master");
            poJournalMaster.initialize();
        }

        return poJournalMaster;
    }
    
    public Model_Journal_Detail Journal_Detail(){
        if (poGRider == null){
            System.err.println("GLModels.Journal_Detail: Application driver is not set.");
            return null;
        }
        
        if (poJournalDetail == null){
            poJournalDetail = new Model_Journal_Detail();
            poJournalDetail.setApplicationDriver(poGRider);
            poJournalDetail.setXML("Model_Journal_Detail");
            poJournalDetail.setTableName("Journal_Detail");
            poJournalDetail.initialize();
        }

        return poJournalDetail;
    }
    
    public Model_Particular Particular(){
        if (poGRider == null){
            System.err.println("GLModels.Particular: Application driver is not set.");
            return null;
        }
        
        if (poParticular == null){
            poParticular = new Model_Particular();
            poParticular.setApplicationDriver(poGRider);
            poParticular.setXML("Model_Particular");
            poParticular.setTableName("Particular");
            poParticular.initialize();
        }

        return poParticular;
    }
    
    public Model_Payee Payee(){
        if (poGRider == null){
            System.err.println("GLModels.Payee: Application driver is not set.");
            return null;
        }
        
        if (poPayee == null){
            poPayee = new Model_Payee();
            poPayee.setApplicationDriver(poGRider);
            poPayee.setXML("Model_Payee");
            poPayee.setTableName("Payee");
            poPayee.initialize();
        }

        return poPayee;
    }
    
    public Model_Recurring_Issuance Recurring_Issuance(){
        if (poGRider == null){
            System.err.println("GLModels.Recurring_Issuance: Application driver is not set.");
            return null;
        }
        
        if (poRecurringIssuance == null){
            poRecurringIssuance = new Model_Recurring_Issuance();
            poRecurringIssuance.setApplicationDriver(poGRider);
            poRecurringIssuance.setXML("Model_Recurring_Issuance");
            poRecurringIssuance.setTableName("Recurring_Issuance");
            poRecurringIssuance.initialize();
        }

        return poRecurringIssuance;
    }
    public Model_Payment_Request_Master PaymentRequestMaster(){
        if (poGRider == null){
            System.err.println("GLModels.PaymentRequestMaster: Application driver is not set.");
            return null;
        }
        
        if (poPaymentRequestMaster == null){
            poPaymentRequestMaster = new Model_Payment_Request_Master();
            poPaymentRequestMaster.setApplicationDriver(poGRider);
            poPaymentRequestMaster.setXML("Model_Payment_Request_Master");
            poPaymentRequestMaster.setTableName("Payment_Request_Master");
            poPaymentRequestMaster.initialize();
        }

        return poPaymentRequestMaster;
    }

    public Model_Payment_Request_Detail PaymentRequestDetail(){
        if (poGRider == null){
            System.err.println("GLModels.PaymentRequestDetail: Application driver is not set.");
            return null;
        }
        
        if (poPaymentRequestDetail == null){
            poPaymentRequestDetail = new Model_Payment_Request_Detail();
            poPaymentRequestDetail.setApplicationDriver(poGRider);
            poPaymentRequestDetail.setXML("Model_Payment_Request_Detail");
            poPaymentRequestDetail.setTableName("Payment_Request_Detail");
            poPaymentRequestDetail.initialize();
        }

        return poPaymentRequestDetail;
    }
    
    private final GRiderCAS poGRider;
    
    private Model_Account_Chart poAccountChart;
    private Model_Transaction_Account_Chart poGeneralLedger;
    private Model_Journal_Master poJournalMaster;
    private Model_Journal_Detail poJournalDetail;
    private Model_Particular poParticular;
    private Model_Payee poPayee;
    private Model_Recurring_Issuance poRecurringIssuance;
    private Model_Payment_Request_Master poPaymentRequestMaster;    
    private Model_Payment_Request_Detail poPaymentRequestDetail;
}
