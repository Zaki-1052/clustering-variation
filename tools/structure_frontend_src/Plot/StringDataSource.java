package Plot;

import jas.hist.*;

class StringDataSource  implements Rebinnable1DHistogramData {
    
    private   double[] data;
    private   String[] labels;
    private   int min,max;
    
    StringDataSource(double[] data, int[] seq) {
	this.data = data;
	labels = new String[data.length];	
	
	
	String bug = new String();
	for(int i=0; i<seq.length; i++){	   
	    labels[i] = new String("ind");
	}
       
	
    }
    
    public double[][] rebin(int   rBins, double rMin,   double rMax, 
			    boolean   wantErrors,   boolean   hurry){
	double[][] result =   { data };
	return result;
    }
    public int getAxisType() { return STRING; }
    public boolean isRebinnable() {   return false; }
    public int getBins() { return data.length; }
     public double getMin() { return   0; }
    public double getMax() { return   0; }
    public String[]   getAxisLabels(){ 
	return labels; 
    }   
    public String getTitle() { return "Result"; }
}
