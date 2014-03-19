var variable = [
    {
      varname:"tas",
      longname:"Near-Surface Air Temperature",
      shortname:"Temperature",
      childs:[{
    	  varname:"tas",
          longname:"Near-Surface Air Temperature",
          shortname:"Temperature"
      	},{
          varname:"tasmin",
          longname:"Daily Minimum Near-Surface Air Temperature",
          shortname:"Min. Temperature"
        },{
          varname:"tasmax",
          longname:"Daily Maximum Near-Surface Air Temperature",
          shortname:"Max. Temperature"
        }
      ]
    },{
      varname:"pr",
      longname:"Total Precipitation",
      shortname:"Rain",
      childs:[{
          varname:"pr",
          longname:"Total Precipitation",
          shortname:"Rain"
      },{
          varname:"prc",
          longname:"Convective Precipitation",
          shortname:"Conv. Precip.",
          category:2
        },
        {
          varname:"prsn",
          longname:"Snowfall flux",
          shortname:"Snow",
          category:2
        }
      ]
    },
    
    {
      varname:"huss",
      longname:"Near-Surface Specific Humidity",
      shortname:"Spec. Humidity",
      childs:[{
          varname:"huss",
          longname:"Near-Surface Specific Humidity",
          shortname:"Spec. Humidity"
      },{
          varname:"hurs",
          longname:"Near-Surface Relative Humidity",
          shortname:"Rel. Humidity",
          category:2
        },
        {
          varname:"hus",
          longname:"Specific Humidity",
          shortname:"Spec. Humidity",
          category:2
        },
        {
          varname:"hur",
          longname:"Relative Humidity",
          shortname:"Rel. Humidity",
          category:2
        },
        {
          varname:"rhs",
          longname:"Daily Near-Surface Specific Humidity",
          shortname:"Rel. Humidity",
          category:2
        },
        {
          varname:"rhsmax",
          longname:"Daily Maximum Near-Surface Relative Humidity",
          shortname:"Max. Rel. Humidity",
          category:2
        },
        {
          varname:"rhsmin",
          longname:"Daily Minimium Near-Surface Relative Humidity",
          shortname:"Min. Rel. Humidity",
          category:2
        }
    ]
    },{
      varname:"sfcWind",
      longname:"Near-Surface Wind Speed",
      shortname:"Wind",
      childs:[{
    	  varname:"sfcWind",
          longname:"Near-Surface Wind Speed",
          shortname:"Wind"
      },{
	      varname:"sfcWindmax",
	      longname:"Daily Maximum Near-Surface Wind Speed",
	      shortname:"Max. Wind"
      },{
	      varname:"uas",
	      longname:"Eastward Near-Surface Wind",
	      shortname:"E. Wind"
      },{
	      varname:"vas",
	      longname:"Northward Near-Surface Wind Speed",
	      shortname:"N. Wind"
      }]
    },{
      varname:"rsds",
      longname:"Surface Downwelling Shortwave Radiation",
      shortname:"SW Radiation",
      childs:[{
    	  varname:"rsds",
    	  longname:"Surface Downwelling Shortwave Radiation",
    	  shortname:"SW Radiation"
      	},{
	      varname:"rsus",
	      longname:"Surface Upwelling Shortwave Radiation",
	      shortname:"SW Up Radiation"
      	},{
	      varname:"rlds",
	      longname:"Surface Downwelling Longwave Radiation",
	      shortname:"LW Radiation"
      	},{
	      varname:"rlus",
	      longname:"Surface Upwelling Longwave Radiation",
	      shortname:"LW Up Radiation"
      	},{
	      varname:"rsdsdiff",
	      longname:"Surface Diffuse Downwelling Shortwave Radiation",
	      shortname:"Diff. Radiation"
      	},{
	      varname:"clt",
	      longname:"Total Cloud Fraction",
	      shortname:"Clouds"
      	}]
    },
    {
      varname:"ps",
      longname:"Surface Air Pressure",
      shortname:"Pressure",
      childs:[{
    	  varname:"ps",
          longname:"Surface Air Pressure",
          shortname:"Pressure"
 	  },{
	      varname:"psl",
	      longname:"Sea-Level Air Pressure",
	      shortname:"SL Pressure"
 	  }]
    },{
	  varname:"evspsbl",
      longname:"Actual Evaporation",
      shortname:"Evaporation",
      childs:[{
	      varname:"evspsbl",
	      longname:"Actual Evaporation",
	      shortname:"Evaporation"
 	  	},{
	      varname:"evspsblpot",
	      longname:"Potential Evaporation",
	      shortname:"Pot. Evaporation"
 	  	}]
    }
]
    



