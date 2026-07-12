package Obj;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;

public class NSimObj {

	//
	// necessary data , important for each run of simulation
	//

	// data comes from project configuration
	// public ProjObj projInfo;

	private NProjObj projObj;
	private String simPath; // the path to the current simulation
	private String simFilePath; // mainparams file path
	private String extFilePath; // extraparams file path

	public boolean isNew = true;

	private String OUTFILE;

	private String simName;

	// the key paramter that could be changed during the runs
	private int MAXPOPS;

	// data in the main parameters panel
	public int BURNIN;
	public int NUMREPS;
	public int SEED = -1;
	//
	// data in the computational model panel
	//

	public boolean FREQSCORR;
	// data related to FREQSCORR
	public boolean ONEFST;
	public float FPRIORMEAN;
	public float FPRIORSD;
	public boolean FQSETLAMBDA;
	public boolean FQINFERLAMBDA;
	public boolean INDIFQSETLAMBDA;
	public boolean INDIFQINFERLAMBDA;

	public boolean NOADMIX;

	// data related to NOADMIX
	public boolean USEDEFAULTADMBURNIN;
	public int ADMBURNIN;

	public boolean INFERALPHA;
	// data related to INFERALPHA
	public float ALPHA;
	public boolean POPALPHAS;
	public boolean UNIFPRIORALPHA;
	public float ALPHAMAX;
	public float ALPHAPROPSD;
	public float ALPHAPRIORA;
	public float ALPHAPRIORB;

	public boolean RECOMBINE;
	// data related to RECOMBINE

	public boolean SITEBYSITE;
	public float RSTART;
	public float RSTEP;
	public float RMAX;
	public float RMIN;
	public float RSTD;
	public boolean MARKOVPHASE;

	public boolean USEPOPINFO;
	// data related to USEPOPINFO
	public int GENSBACK;
	public float MIGRPRIOR;
	// for missing popinfo individuals, use which model
	public boolean POPNOADMIX;
	public boolean POPRECOMBINE;
	//
	// data in the additional parameters model panel
	//

	public boolean INFERLAMBDA;
	// data related to INFERLAMBDA
	public boolean POPSPECIFICLAMBDA;
	public float LAMBDA;

	public boolean COMPUTEPROB;
	public boolean ANCESTDIST;
	public boolean PRINTQ;

	public int NUMBOXES;
	public float ANCESTPINT;
	public boolean STARTPOPINFO;
	public int METROFREQ;

	// added in June 2003, final release of version2
	public boolean PFROMPOPFLAGONLY;

	// added in Feb 2009, priors for sampling location
	public boolean LOCPRIOR;

	public NSimObj(NProjObj projObj) {

		this.projObj = projObj;

	}

	public NSimObj(SimObj oldsim) {
		this.projObj = new NProjObj(oldsim.getProjObj());
		setSimPath(oldsim.getSimPath());
		this.simName = oldsim.getSimName();
		this.isNew = false;
		this.BURNIN = oldsim.BURNIN;
		this.NUMREPS = oldsim.NUMREPS;
		this.FREQSCORR = oldsim.FREQSCORR;
		this.ONEFST = oldsim.ONEFST;
		this.FPRIORMEAN = oldsim.FPRIORMEAN;
		this.FPRIORSD = oldsim.FPRIORSD;
		this.FQSETLAMBDA = oldsim.FQSETLAMBDA;
		this.FQINFERLAMBDA = oldsim.FQINFERLAMBDA;
		this.INDIFQSETLAMBDA = oldsim.INDIFQSETLAMBDA;
		this.INDIFQINFERLAMBDA = oldsim.INDIFQINFERLAMBDA;
		this.NOADMIX = oldsim.NOADMIX;
		this.USEDEFAULTADMBURNIN = oldsim.USEDEFAULTADMBURNIN;
		this.ADMBURNIN = oldsim.ADMBURNIN;
		this.INFERALPHA = oldsim.INFERALPHA;
		this.ALPHA = oldsim.ALPHA;
		this.POPALPHAS = oldsim.POPALPHAS;
		this.UNIFPRIORALPHA = oldsim.UNIFPRIORALPHA;
		this.ALPHAMAX = oldsim.ALPHAMAX;
		this.ALPHAPROPSD = oldsim.ALPHAPROPSD;
		this.ALPHAPRIORA = oldsim.ALPHAPRIORA;
		this.ALPHAPRIORB = oldsim.ALPHAPRIORB;
		this.RECOMBINE = oldsim.RECOMBINE;
		this.SITEBYSITE = oldsim.SITEBYSITE;
		this.RSTART = oldsim.RSTART;
		this.RSTEP = oldsim.RSTEP;
		this.RMAX = oldsim.RMAX;
		this.RMIN = oldsim.RMIN;
		this.RSTD = oldsim.RSTD;
		this.MARKOVPHASE = oldsim.MARKOVPHASE;
		this.USEPOPINFO = oldsim.USEPOPINFO;
		this.GENSBACK = oldsim.GENSBACK;
		this.MIGRPRIOR = oldsim.MIGRPRIOR;
		this.POPNOADMIX = oldsim.POPNOADMIX;
		this.POPRECOMBINE = oldsim.POPRECOMBINE;
		this.INFERLAMBDA = oldsim.INFERLAMBDA;
		this.POPSPECIFICLAMBDA = oldsim.POPSPECIFICLAMBDA;
		this.LAMBDA = oldsim.LAMBDA;
		this.COMPUTEPROB = oldsim.COMPUTEPROB;
		this.ANCESTDIST = oldsim.ANCESTDIST;
		this.PRINTQ = oldsim.PRINTQ;
		this.NUMBOXES = oldsim.NUMBOXES;
		this.ANCESTPINT = oldsim.ANCESTPINT;
		this.STARTPOPINFO = oldsim.STARTPOPINFO;
		this.METROFREQ = oldsim.METROFREQ;

		this.PFROMPOPFLAGONLY = false;
	}

	public String getConfig() {

		String str = new String("#define OUTFILE " + OUTFILE + "\n");
		str += projObj.getConfig();

		// ///////////////////////////////////////////
		// //
		// print the Running Length //
		// //
		// ///////////////////////////////////////////

		str += new String("#define MAXPOPS " + MAXPOPS + "\n");
		str += new String("#define BURNIN " + BURNIN + "\n");
		str += new String("#define NUMREPS " + NUMREPS + "\n\n");

		// //////////// Random Seed ///////////////

		if (SEED != -1) {
			str += new String("#define RANDOMIZE 0\n");
			str += new String("#define SEED " + SEED + "\n\n");
		}

		// ///////////////////////////////////////////
		// //
		// print the ancestry model parameters //
		// //
		// ///////////////////////////////////////////

		if (USEPOPINFO) {
			str += new String("\n\n#define USEPOPINFO 1 \n");
			if (!POPRECOMBINE) {
				str += new String("#define GENSBACK " + GENSBACK + "\n");
				str += new String("#define MIGRPRIOR " + MIGRPRIOR + "\n");
			} else {
				str += new String("#define GENSBACK  0 \n");
				str += new String("#define MIGRPRIOR  0 \n");
			}
			// for individuals without popinfo data, the default model is
			if (POPRECOMBINE) {
				str += new String("\n#define LINKAGE 1 \n");
				str += new String("#define NOADMIX 0 \n");
				if (SITEBYSITE) {
					str += new String("#define SITEBYSITE 1 \n");
				} else {
					str += new String("#define SITEBYSITE 0 \n");
				}

				if (MARKOVPHASE) {
					str += new String("#define MARKOVPHASE 1 \n");
				} else {
					str += new String("#define MARKOVPHASE 0 \n");
				}

				str += new String("#define LOG10RSTART " + RSTART + "\n");
				str += new String("#define LOG10RMAX " + RMAX + "\n");
				str += new String("#define LOG10RMIN " + RMIN + "\n");
				str += new String("#define LOG10RPROPSD " + RSTD + "\n");

				if (USEDEFAULTADMBURNIN) {
					str += new String("#define ADMBURNIN " + BURNIN / 2 + "\n");
				} else {
					str += new String("#define ADMBURNIN " + ADMBURNIN + "\n");
				}

			} else if (!POPNOADMIX) {
				str += new String("\n#define NOADMIX 0\n");
				str += new String("\n#define LINKAGE 0\n");
				if (INFERALPHA) {
					str += new String("#define INFERALPHA 1\n");
					str += new String("#define ALPHA " + ALPHA + "\n");
					if (POPALPHAS) {
						str += new String("#define POPALPHAS 1 \n");
					} else {
						str += new String("#define POPALPHAS 0 \n");
					}
					if (UNIFPRIORALPHA) {
						str += new String("#define UNIFPRIORALPHA 1 \n");
						str += new String("#define ALPHAMAX " + ALPHAMAX + "\n");
						str += new String("#define ALPHAPROPSD " + ALPHAPROPSD
								+ "\n");
					} else {
						str += new String("#define UNIFPRIORALPHA 0 \n");
						str += new String("#define ALPHAPRIORA " + ALPHAPRIORA
								+ "\n");
						str += new String("#define ALPHAPRIORB " + ALPHAPRIORB
								+ "\n");

					}
				} else {
					str += new String("#define INFERALPHA 0\n");
					str += new String("#define ALPHA " + ALPHA + "\n");
				}
			} else {
				str += new String("\n#define LINKAGE 0\n");
				str += new String("#define NOADMIX 1\n");
				str += new String("#define ADMBURNIN " + BURNIN / 2 + "  \n");
			}
		} else if (RECOMBINE) {

			str += new String("\n#define LINKAGE 1 \n");
			str += new String("#define NOADMIX 0 \n");
			str += new String("#define USEPOPINFO 0 \n");
			if (SITEBYSITE) {
				str += new String("#define SITEBYSITE 1 \n");
			} else {
				str += new String("#define SITEBYSITE 0 \n");
			}

			if (MARKOVPHASE) {
				str += new String("#define MARKOVPHASE 1 \n");
			} else {
				str += new String("#define MARKOVPHASE 0 \n");
			}

			str += new String("#define LOG10RSTART " + RSTART + "\n");
			str += new String("#define LOG10RMAX " + RMAX + "\n");
			str += new String("#define LOG10RMIN " + RMIN + "\n");
			str += new String("#define LOG10RPROPSD " + RSTD + "\n");

			if (USEDEFAULTADMBURNIN) {
				str += new String("#define ADMBURNIN " + BURNIN / 2 + "\n");
			} else {
				str += new String("#define ADMBURNIN " + ADMBURNIN + "\n");
			}

		} else if (!NOADMIX) {
			str += new String("\n#define NOADMIX 0\n");
			str += new String("#define LINKAGE 0\n");
			str += new String("#define USEPOPINFO 0\n");
			// location prior
			if (LOCPRIOR) {
				str += new String("\n#define LOCPRIOR 1\n");
				if (!projObj.getLocData() && projObj.getPopId()) {
					str += new String("\n#define LOCISPOP 1\n");
				}
			} else {
				str += new String("\n#define LOCPRIOR 0\n");
			}
			if (INFERALPHA) {
				str += new String("#define INFERALPHA 1\n");
				str += new String("#define ALPHA " + ALPHA + "\n");
				if (POPALPHAS) {
					str += new String("#define POPALPHAS 1 \n");
				} else {
					str += new String("#define POPALPHAS 0 \n");
				}
				if (UNIFPRIORALPHA) {
					str += new String("#define UNIFPRIORALPHA 1 \n");
					str += new String("#define ALPHAMAX " + ALPHAMAX + "\n");
					str += new String("#define ALPHAPROPSD " + ALPHAPROPSD
							+ "\n");
				} else {
					str += new String("#define UNIFPRIORALPHA 0 \n");
					str += new String("#define ALPHAPRIORA " + ALPHAPRIORA
							+ "\n");
					str += new String("#define ALPHAPRIORB " + ALPHAPRIORB
							+ "\n");

				}
			} else {
				str += new String("#define INFERALPHA 0\n");
				str += new String("#define ALPHA " + ALPHA + "\n");
			}
		} else {
			str += new String("\n#define LINKAGE 0\n");
			str += new String("#define NOADMIX 1\n");
			str += new String("#define ADMBURNIN " + BURNIN / 2 + "  \n");
			str += new String("#define USEPOPINFO 0\n");
			if (LOCPRIOR) {
				str += new String("\n#define LOCPRIOR 1\n");
				if (!projObj.getLocData() && projObj.getPopId()) {
					str += new String("\n#define LOCISPOP 1\n");
				}
			} else {
				str += new String("\n#define LOCPRIOR 0\n");
			}
		}

		// ///////////////////////////////////////////
		// //
		// print the frequency model parameters //
		// //
		// ///////////////////////////////////////////

		if (INFERLAMBDA) {

			str += new String("\n\n#define INFERLAMBDA 1 \n");
			str += new String("\n#define FREQSCORR 0\n");

			if (POPSPECIFICLAMBDA) {
				str += new String("#define POPSPECIFICLAMBDA 1 \n");
			} else {
				str += new String("#define POPSPECIFICLAMBDA 0 \n");
			}

			str += new String("#define LAMBDA " + LAMBDA + "\n");

		} else if (FREQSCORR) {
			str += new String("\n\n#define FREQSCORR 1 \n");
			if (ONEFST) {
				str += new String("#define ONEFST 1\n");
			} else {
				str += new String("#define ONEFST 0\n");
			}

			str += new String("#define FPRIORMEAN " + FPRIORMEAN + "\n");
			str += new String("#define FPRIORSD " + FPRIORSD + "\n");
			if (FQINFERLAMBDA) {
				str += new String("\n\n#define INFERLAMBDA 1 \n");
				if (POPSPECIFICLAMBDA) {
					str += new String("#define POPSPECIFICLAMBDA 1 \n");
				} else {
					str += new String("#define POPSPECIFICLAMBDA 0 \n");
				}

				str += new String("#define LAMBDA " + LAMBDA + "\n");

			} else if (FQSETLAMBDA) {
				str += new String("\n\n#define INFERLAMBDA 0 \n");
				str += new String("#define LAMBDA " + LAMBDA + "\n");
			}
		} else {
			str += new String("\n#define FREQSCORR 0\n");
			if (INDIFQINFERLAMBDA) {
				str += new String("\n\n#define INFERLAMBDA 1 \n");
				if (POPSPECIFICLAMBDA) {
					str += new String("#define POPSPECIFICLAMBDA 1 \n");
				} else {
					str += new String("#define POPSPECIFICLAMBDA 0 \n");
				}

				str += new String("#define LAMBDA " + LAMBDA + "\n");
			} else if (INDIFQSETLAMBDA) {
				str += new String("#define LAMBDA " + LAMBDA + "\n");
			}
		}

		// ///////////////////////////////////////////
		// //
		// print additonal computation parameters //
		// //
		// ///////////////////////////////////////////

		if (COMPUTEPROB) {
			str += new String("#define COMPUTEPROB 1 \n");
		} else {
			str += new String("#define COMPUTEPROB 0 \n");
		}
		if (PFROMPOPFLAGONLY) {
			str += new String("#define PFROMPOPFLAGONLY 1 \n");
		} else {
			str += new String("#define PFROMPOPFLAGONLY 0 \n");
		}
		if (ANCESTDIST) {
			str += new String("#define ANCESTDIST 1 \n");
			str += new String("#define NUMBOXES " + NUMBOXES + "\n");
			str += new String("#define ANCESTPINT " + ANCESTPINT + "\n");
		} else {
			str += new String("#define ANCESTDIST 0 \n");
		}

		if (STARTPOPINFO) {
			str += new String("#define STARTATPOPINFO 1 \n");
		} else {
			str += new String("#define STARTATPOPINFO 0 \n");
		}

		str += new String("#define METROFREQ " + METROFREQ + "\n\n\n");

		str += new String("#define UPDATEFREQ 1 \n");

		if (PRINTQ) {
			str += new String("#define PRINTQHAT 1\n");
		}

		return str;
	}

	public String printSimInfo() {

		String str = new String("");
		str += "\n";
		str += "         ====================  Parameter Set: " + getSimName()
				+ "  ====================     \n\n\n";

		str += "                                    Running Length          \n\n";

		str += "                     Length of Burnin Period: " + BURNIN + "\n";
		str += "                     Number of MCMC Reps after Burnin: "
				+ NUMREPS + "\n\n\n";

		str += "                                    Ancestry Model Info          \n\n";

		if (USEPOPINFO) {
			str += "                   Use Prior Population Information to Assist Clustering           \n";
			if (!POPRECOMBINE) {
				str += "                     * GENSBACK  = " + GENSBACK + "\n";
				str += "                     * MIGRPRIOR = " + MIGRPRIOR + "\n";
			}
			str += "                     * For Individuals without population information data, use    \n";
			// for individuals without popinfo data, the default model is
			if (POPRECOMBINE) {
				str += "                          LinkageModel          \n";

				if (SITEBYSITE) {
					str += "                            ** Print site-by-site information     \n";
				}
				if (MARKOVPHASE) {
					str += "                            ** The phase info follows a Markov model";
				}

				str += "                            ** LOG10(RSTART) = "
						+ RSTART + "\n";
				str += "                            ** LOG10(RMAX) = " + RMAX
						+ "\n";
				str += "                            ** LOG10(RMIN) = " + RMIN
						+ "\n";
				str += "                            ** LOG10(RPROPSD) = "
						+ RSTD + "\n";

				if (USEDEFAULTADMBURNIN) {
					str += "                            ** Admixture burnin length: "
							+ BURNIN / 2 + "\n";
				} else {
					str += "                            ** Admixture burnin length: "
							+ ADMBURNIN + "\n";
				}

			} else if (!POPNOADMIX) {
				str += "                          Admixture Model            \n";

				if (INFERALPHA) {

					str += "                                ** Infer Alpha\n";
					str += "                                ** Initial Value of ALPHA (Dirichlet Parameter for Degree of Admixture):  "
							+ ALPHA + "\n";
					if (POPALPHAS) {
						str += "                                ** Use Individual Alpha for Each Population\n";
					} else {
						str += "                                ** Use Same Alpha for all Populations\n";
					}
					if (UNIFPRIORALPHA) {
						str += "                                ** Use a Uniform Prior for Alpha\n";
						str += "                                  *** Maximum Value for Alpha: "
								+ ALPHAMAX + "\n";
						str += "                                  *** SD of Proposal for Updating Alpha: "
								+ ALPHAPROPSD + "\n";
					} else {
						str += "                                ** Use a Gamma Prior for Alpha \n";
						str += "                                  *** A: "
								+ ALPHAPRIORA + "\n";
						str += "                                  *** B: "
								+ ALPHAPRIORB + "\n";
					}
				} else {
					str += "                                ** Use Constant Alpha Value\n";
					str += "                                ** Value of Alpha (Dirichlet Parameter for Defree of Admixture): "
							+ ALPHA + "\n";
				}
			} else {
				str += "                          No Admixture Model                 \n\n";

			}
		} else if (RECOMBINE) {

			str += "                    Use Linkage Model   \n";
			if (SITEBYSITE) {
				str += "                      * Print site-by-site information     \n";
			}
			if (MARKOVPHASE) {
				str += "                      * The phase info follows a Markov model   \n";
			}
			str += "                      * LOG10(RSTART) = " + RSTART + "\n";
			str += "                      * LOG10(RMAX) = " + RMAX + "\n";
			str += "                      * LOG10(RMIN) = " + RMIN + "\n";
			str += "                      * LOG10(RPROPSD) = " + RSTD + "\n";

			if (USEDEFAULTADMBURNIN) {
				str += "                      * Admixture burnin length: "
						+ BURNIN / 2 + "\n";
			} else {
				str += "                      * Admixture burnin length: "
						+ ADMBURNIN + "\n";
			}

		} else if (!NOADMIX) {
			str += "                    Use Admixture Model       \n";
			if (LOCPRIOR) {
				str += "                      * Use Sampling Location Information\n";
				if (!projObj.getLocData() && projObj.getPopId()) {
					str += "                      * Use Population IDs as Sampling Location Information\n";
				}
			}
			if (INFERALPHA) {
				str += "                      * Infer Alpha\n";
				str += "                      * Initial Value of ALPHA (Dirichlet Parameter for Degree of Admixture):  "
						+ ALPHA + "\n";
				if (POPALPHAS) {
					str += "                      * Use Individual Alpha for Each Population\n";
				} else {
					str += "                      * Use Same Alpha for all Populations\n";
				}
				if (UNIFPRIORALPHA) {
					str += "                      * Use a Uniform Prior for Alpha\n";
					str += "                         ** Maximum Value for Alpha: "
							+ ALPHAMAX + "\n";
					str += "                         ** SD of Proposal for Updating Alpha: "
							+ ALPHAPROPSD + "\n";
				} else {
					str += "                      * Use a Gamma Prior for Alpha \n";
					str += "                         ** A: " + ALPHAPRIORA
							+ "\n";
					str += "                         ** B: " + ALPHAPRIORB
							+ "\n";
				}
			} else {
				str += "                      * Use Constant Alpha Value\n";
				str += "                      * Value of Alpha (Dirichlet Parameter for Defree of Admixture): "
						+ ALPHA + "\n";
			}

		} else {
			str += "                  Use No Admixture Model       \n\n";
			if (LOCPRIOR) {
				str += "                      * Use Sampling Location Information\n";
				if (!projObj.getLocData() && projObj.getPopId()) {
					str += "                      * Use Population IDs as Sampling Location Information\n";
				}
			}
		}
		str += "\n\n";
		str += "                                    Frequency Model Info   \n\n";

		if (INFERLAMBDA) {
			str += "                     Infer Lambda (Allele Frequencies Parameter) \n";
			if (POPSPECIFICLAMBDA) {
				str += "                       * Infer a Separate Lambda for each Population\n";
			} else {
				str += "                       * Use a Uniform Lambda for All Population\n";
			}

			str += "                       * Initial Value of Lambda: "
					+ LAMBDA + "\n";

		} else if (FREQSCORR) {
			str += "                     Allele Frequencies are Correlated among Pops\n";
			if (ONEFST) {
				str += "                       * Assume Same Value of Fst for All Subpopulations\n";
			} else {
				str += "                       * Assume Different Values of Fst for Different Subpopulations\n";
			}

			str += "                       * Prior Mean of Fst for Pops: "
					+ FPRIORMEAN + "\n";
			str += "                       * Prior SD   of Fst for Pops: "
					+ FPRIORSD + "\n";

			if (FQINFERLAMBDA) {
				str += "                       * Infer LAMBDA  \n";
				if (POPSPECIFICLAMBDA) {
					str += "                         ** Infer a Separate Lambda for each Population\n";
				} else {
					str += "                         ** Use a Uniform Lambda for All Population\n";
				}

				str += "                         ** Initial Value of Lambda: "
						+ LAMBDA + "\n";
			} else if (FQSETLAMBDA) {
				str += "                       * Use Constant Lambda (Allele Frequencies Parameter) \n";
				str += "                       * Value of Lambda: " + LAMBDA
						+ "\n";
			}
		} else {
			str += "                     Allele Frequencies are Independent among Pops\n";
			if (INDIFQINFERLAMBDA) {
				str += "                       * Infer LAMBDA  \n";
				if (POPSPECIFICLAMBDA) {
					str += "                         ** Infer a Separate Lambda for each Population\n";
				} else {
					str += "                         ** Use a Uniform Lambda for All Population\n";
				}

				str += "                         ** Initial Value of Lambda: "
						+ LAMBDA + "\n";
			} else if (INDIFQSETLAMBDA) {
				str += "                       * Use Constant Lambda (Allele Frequencies Parameter) \n";
				str += "                       * Value of Lambda: " + LAMBDA
						+ "\n";

			}
		}

		str += "\n\n";
		str += "                                    Advanced Options   \n\n";

		if (PFROMPOPFLAGONLY) {
			str += "                      Update Allele Frequencies using Individuals with POPFLAG=1 ONLY\n";
		}
		if (COMPUTEPROB) {
			str += "                      Estimate the Probability of the Data Under the Model \n";
		}

		if (ANCESTDIST) {
			str += "                      Collect Data About the Distribution of Ancestry Coefficients Q for each Individual\n";
			str += "                        * Distribution of Q values is Stored as a Histogram with "
					+ NUMBOXES + " Boxes\n";
			str += "                        * Size of the Displayed Probability Interval on Q: "
					+ ANCESTPINT + "\n";
		}

		if (PRINTQ) {
			str += "                      Print Q-hat\n";
		}
		if (STARTPOPINFO) {
			str += "                      Initialize at POPINFO \n";
		}

		str += "                      Frequency of Metropolis update for Q: "
				+ METROFREQ + "\n\n\n";

		return str;
	}

	// the result name
	public String getRstName() {

		File f = new File(OUTFILE);
		return f.getName();
	}

	public boolean isNew() {
		return isNew;
	}

	public boolean createSimSpace() {

		String projPath = projObj.getWorkingPath();

		while (true) {
			simName = JOptionPane
					.showInputDialog("Please name the new parameter set ");

			if (simName == null) {
				return false;
			}
			StringTokenizer st = new StringTokenizer(simName);

			if (!st.hasMoreTokens()) {
				continue;
			}
			
			
			// if the filename looks too long
			if (30 + 2*simName.length() + projPath.length() >= 255) {
				// check the longest name we should get
				File tmpFile = new File(projPath, simName + "_PlotData_"+simName+"_run_XXX_alphahist");
				boolean okFile = true;
				try {
					tmpFile.createNewFile();
					okFile = tmpFile.canWrite();
					tmpFile.delete();				
				} catch (IOException e) {
					okFile = false;
				}
				if (!okFile) {
					JOptionPane.showMessageDialog(null,  "The file name \n"+projPath+"/"+simName+"\n is too long", "Filename too long", JOptionPane.ERROR_MESSAGE, null);
					return false;
				}
			}
			
			if (projObj.checkSimName(simName)) {
				break;
			}
			JOptionPane.showMessageDialog(null,
					"The parameter set with name " + simName
							+ " already exists ", "Structure",
					JOptionPane.ERROR_MESSAGE);
		}

		File simDir = new File(projPath, simName);
		simDir.mkdirs();

		simPath = simDir.getAbsolutePath();

		File resultDir = new File(simPath, "Results");
		if (!resultDir.exists()) {
			resultDir.mkdirs();
		}
		File plotDir = new File(simPath, "PlotData");
		if (!plotDir.exists()) {
			plotDir.mkdirs();
		}
		isNew = false;

		return true;
	}

	public String getSimName() {
		return simName;
	}

	public void setSimName(String simName) {
		this.simName = simName;
	}

	public String getSimPath() {
		return simPath;
	}

	// reset simpath every time simObj is loaded
	public void setSimPath(String path) {
		simPath = path;
	}

	public boolean setOutFile(String filename) {
		File resultDir = new File(simPath, "Results");
		String testname = filename + "_f";
		File targetFile = new File(resultDir.getAbsolutePath(), testname);
		if (targetFile.exists()) {
			return false;
		}

		OUTFILE = (new File(resultDir.getAbsolutePath(), filename))
				.getAbsolutePath();
		return true;
	}

	public void setMAXPOPS(int K) {
		MAXPOPS = K;
	}

	public void setRNDSEED(int seed) {
		SEED = seed;
	}

	public int getMAXPOPS() {

		return MAXPOPS;

	}

	public void writeParamFile() {

		File paramFile = new File(simPath, "mainparams");
		PrintStream out = OpenOutputFile(paramFile);
		out.println(getConfig());
		simFilePath = paramFile.getAbsolutePath();

		// create an empty extraparam file
		File extraFile = new File(simPath, "extraparams");
		PrintStream exout = OpenOutputFile(extraFile);
		exout.println(" ");
		extFilePath = extraFile.getAbsolutePath();

	}

	public String getParamFile() {
		return simFilePath;
	}

	public String getExtraFile() {
		return extFilePath;
	}

	private static PrintStream OpenOutputFile(File file) {

		PrintStream out;
		try { // try to open output file as well...
			out = new PrintStream(new FileOutputStream(file));
		} catch (FileNotFoundException e) {
			System.err.println("**Error: unable to open output file.");
			out = null;
		}// try-catch
		catch (SecurityException e) {
			System.err.println("**Error: no permission to write output file.");
			out = null;
		}// try-catch


		return out;
	}

	public boolean equals(NSimObj obj) {

		return (BURNIN == obj.BURNIN && NUMREPS == obj.NUMREPS
				&& FREQSCORR == obj.FREQSCORR && ONEFST == obj.ONEFST
				&& FPRIORMEAN == obj.FPRIORMEAN && FPRIORSD == obj.FPRIORSD
				&& NOADMIX == obj.NOADMIX && ADMBURNIN == obj.ADMBURNIN
				&& INFERALPHA == obj.INFERALPHA && ALPHA == obj.ALPHA
				&& POPALPHAS == obj.POPALPHAS
				&& UNIFPRIORALPHA == obj.UNIFPRIORALPHA
				&& ALPHAMAX == obj.ALPHAMAX && ALPHAPROPSD == obj.ALPHAPROPSD
				&& ALPHAPRIORA == obj.ALPHAPRIORA
				&& ALPHAPRIORB == obj.ALPHAPRIORB && RECOMBINE == obj.RECOMBINE
				&& SITEBYSITE == obj.SITEBYSITE && RSTART == obj.RSTART
				&& RMAX == obj.RMAX && RMIN == obj.RMIN && RSTD == obj.RSTD
				&& USEPOPINFO == obj.USEPOPINFO && GENSBACK == obj.GENSBACK
				&& MIGRPRIOR == obj.MIGRPRIOR && INFERLAMBDA == obj.INFERLAMBDA
				&& POPSPECIFICLAMBDA == obj.POPSPECIFICLAMBDA
				&& LAMBDA == obj.LAMBDA && COMPUTEPROB == obj.COMPUTEPROB
				&& ANCESTDIST == obj.ANCESTDIST && NUMBOXES == obj.NUMBOXES
				&& ANCESTPINT == obj.ANCESTPINT
				&& STARTPOPINFO == obj.STARTPOPINFO
				&& METROFREQ == obj.METROFREQ
				&& INDIFQSETLAMBDA == obj.INDIFQSETLAMBDA
				&& INDIFQINFERLAMBDA == obj.INDIFQINFERLAMBDA
				&& FQSETLAMBDA == obj.FQSETLAMBDA
				&& FQINFERLAMBDA == obj.FQINFERLAMBDA
				&& USEDEFAULTADMBURNIN == obj.USEDEFAULTADMBURNIN
				&& POPNOADMIX == obj.POPNOADMIX
				&& POPRECOMBINE == obj.POPRECOMBINE && PRINTQ == obj.PRINTQ
				&& PFROMPOPFLAGONLY == obj.PFROMPOPFLAGONLY && LOCPRIOR == obj.LOCPRIOR);

	}

	public void setProjObj(NProjObj proj) {
		this.projObj = proj;
	}

	public NProjObj getProjObj() {
		return projObj;
	}

}
