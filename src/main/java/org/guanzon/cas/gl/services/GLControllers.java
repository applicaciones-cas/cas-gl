package org.guanzon.cas.gl.services;

import java.sql.SQLException;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.LogWrapper;
import org.guanzon.cas.gl.AccountChart;

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
    
    @Override
    protected void finalize() throws Throwable {
        try {                    
            poAccountChart = null;
            
            poLogWrapper = null;
            poGRider = null;
        } finally {
            super.finalize();
        }
    }
    
    private GRiderCAS poGRider;
    private LogWrapper poLogWrapper;
    
    private AccountChart poAccountChart;
}
