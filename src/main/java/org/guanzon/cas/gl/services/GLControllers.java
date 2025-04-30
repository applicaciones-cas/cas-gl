package org.guanzon.cas.gl.services;

import java.sql.SQLException;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.LogWrapper;
import org.guanzon.cas.gl.AccountChart;
import org.guanzon.cas.gl.Particular;
import org.guanzon.cas.gl.Payee;
import org.guanzon.cas.gl.PaymentRequest;
import org.guanzon.cas.gl.RecurringIssuance;
import org.guanzon.cas.gl.TransactionAccountChart;

public class GLControllers {
    public GLControllers(GRiderCAS applicationDriver, LogWrapper logWrapper){
        poGRider = applicationDriver;
        poLogWrapper = logWrapper;
    }
    
    public AccountChart AccountChart() throws SQLException, GuanzonException{
        if (poGRider == null){
            poLogWrapper.severe("GLControllers.AccountChart: Application driver is not set.");
            return null;
        }
        
        if (poAccountChart != null) return poAccountChart;
        
        poAccountChart = new AccountChart();
        poAccountChart.setApplicationDriver(poGRider);
        poAccountChart.setWithParentClass(false);
        poAccountChart.setLogWrapper(poLogWrapper);
        poAccountChart.initialize();
        poAccountChart.newRecord();
        return poAccountChart;        
    }
    
    public TransactionAccountChart TransactionAccountChart() throws SQLException, GuanzonException{
        if (poGRider == null){
            poLogWrapper.severe("GLControllers.TransactionAccountChart: Application driver is not set.");
            return null;
        }
        
        if (poGeneralLedger != null) return poGeneralLedger;
        
        poGeneralLedger = new TransactionAccountChart();
        poGeneralLedger.setApplicationDriver(poGRider);
        poGeneralLedger.setWithParentClass(false);
        poGeneralLedger.setLogWrapper(poLogWrapper);
        poGeneralLedger.initialize();
        poGeneralLedger.newRecord();
        return poGeneralLedger;        
    }
    
    public Payee Payee() throws SQLException, GuanzonException{
        if (poGRider == null){
            poLogWrapper.severe("GLControllers.Payee: Application driver is not set.");
            return null;
        }
        
        if (poPayee != null) return poPayee;
        
        poPayee = new Payee();
        poPayee.setApplicationDriver(poGRider);
        poPayee.setWithParentClass(false);
        poPayee.setLogWrapper(poLogWrapper);
        poPayee.initialize();
        poPayee.newRecord();
        return poPayee;        
    }
    
    public Particular Particular() throws SQLException, GuanzonException{
        if (poGRider == null){
            poLogWrapper.severe("GLControllers.Particular: Application driver is not set.");
            return null;
        }
        
        if (poParticular != null) return poParticular;
        
        poParticular = new Particular();
        poParticular.setApplicationDriver(poGRider);
        poParticular.setWithParentClass(false);
        poParticular.setLogWrapper(poLogWrapper);
        poParticular.initialize();
        poParticular.newRecord();
        return poParticular;        
    }
    
    public RecurringIssuance RecurringIssuance() throws SQLException, GuanzonException{
        if (poGRider == null){
            poLogWrapper.severe("GLControllers.RecurringIssuance: Application driver is not set.");
            return null;
        }
        
        if (poRecurringIssuance != null) return poRecurringIssuance;
        
        poRecurringIssuance = new RecurringIssuance();
        poRecurringIssuance.setApplicationDriver(poGRider);
        poRecurringIssuance.setWithParentClass(false);
        poRecurringIssuance.setLogWrapper(poLogWrapper);
        poRecurringIssuance.initialize();
//        poRecurringIssuance.newRecord();
        return poRecurringIssuance;        
    }
    
    public PaymentRequest PaymentRequest() throws SQLException, GuanzonException{
        if (poGRider == null){
            poLogWrapper.severe("GLControllers.Particular: Application driver is not set.");
            return null;
        }
        
        if (poPaymentRequest != null) return poPaymentRequest;
        
        poPaymentRequest = new PaymentRequest();
        poPaymentRequest.setApplicationDriver(poGRider);
        poPaymentRequest.setLogWrapper(poLogWrapper);
        return poPaymentRequest;        
    }

       
    @Override
    protected void finalize() throws Throwable {
        try {                    
            poAccountChart = null;
            poGeneralLedger = null;
            poParticular = null;
            poPayee = null;
            poRecurringIssuance = null;
            
            poLogWrapper = null;
            poGRider = null;
        } finally {
            super.finalize();
        }
    }
    
    private GRiderCAS poGRider;
    private LogWrapper poLogWrapper;
    
    private AccountChart poAccountChart;
    private TransactionAccountChart poGeneralLedger;
    private Particular poParticular;
    private Payee poPayee;
    private RecurringIssuance poRecurringIssuance;
    private PaymentRequest poPaymentRequest;
}
