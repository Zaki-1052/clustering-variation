package Plot;

import jas.hist.*;

class ArrayDataSource implements Rebinnable1DHistogramData
{
    private double min;
    private double max;

    ArrayDataSource(double[] data){
	this(data,"Array Data Source");
    }
    
    ArrayDataSource(double[] data,double min, double max)
    {
	this(data,"Array Data Source");
	this.min = min;
	this.max = max;
    }
    ArrayDataSource(double[] data, String name)
    {
	this.data =   data;
	this.name =   name;
    }

    ArrayDataSource(double[] data, String name, double min, double max)
    {
	this.data =   data;
	this.name =   name;
	this.min  =    min;
	this.max  =    max;
    }
    
    public double[][] rebin(int   rBins, double rMin,   double 
			    rMax, 
			    boolean   wantErrors,   boolean   hurry)
    {
	double[][] result =   { data };
	return result;
    }
    public String[]   getAxisLabels()   { return null; }
    public double getMin() { return   min; }
    public double getMax() { return   max; }
    public boolean isRebinnable() {   return false; }
    public int getBins() { return data.length; }
    public int getAxisType() { return DOUBLE; }
    public String getTitle() { return name;   }
    private   double[] data;
    private   String name;
}


