package org.guanzon.cas.gl.services;

import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.cas.gl.model.Model_Account_Chart;
import org.guanzon.cas.gl.model.Model_Journal_Detail;
import org.guanzon.cas.gl.model.Model_Journal_Master;
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
            poJournalMaster.setTableName("Model_Journal_Master");
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
            poJournalDetail.setTableName("Model_Journal_Detail");
            poJournalDetail.initialize();
        }

        return poJournalDetail;
    }
    
    private final GRiderCAS poGRider;
    
    private Model_Account_Chart poAccountChart;
    private Model_Transaction_Account_Chart poGeneralLedger;
    private Model_Journal_Master poJournalMaster;
    private Model_Journal_Detail poJournalDetail;
}
