package org.guanzon.cas.gl.services;

import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.cas.gl.model.Model_Account_Chart;
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
    
    private final GRiderCAS poGRider;
    
    private Model_Account_Chart poAccountChart;
    private Model_Transaction_Account_Chart poGeneralLedger;
}
