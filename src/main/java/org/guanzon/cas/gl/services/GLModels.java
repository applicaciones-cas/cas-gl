package org.guanzon.cas.gl.services;

import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.cas.gl.model.Model_Account_Chart;

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
    
    private final GRiderCAS poGRider;
    
    private Model_Account_Chart poAccountChart;
}
