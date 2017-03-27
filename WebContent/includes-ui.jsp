<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="impactservice.Configuration"%>   
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <title>Climate impact portal | For exploration, visualization and analysis of climate model data</title>
    <meta http-equiv="X-UA-Compatible" content="IE=EmulateIE8" >
    <meta charset="UTF-8">
    <meta name="description" content="The aim of Climate4impact (C4I) is to enhance the use of research data and to support other climate portals. It has been developed within the European projects IS-ENES, IS-ENES2 and CLIPC. Climate4impact is connected to the Earth System Grid Federation, using certificate based authentication, ESGF search, openid, opendap and thredds catalogs. Climate4impact offers web interfaces for searching, visualizing, analyzing, processing and downloading datasets.">
    <meta name="keywords" content="climate4impact, climateimpact, impactportal, SPECS, CMIP5, CMIP6, CORDEX, CLIPC, IS-ENES, WMS, WPS, WCS, ADAGUC, ICCLIM, impact, ESGF, onlineanalysis, research, webprocessing">

    <link rel="icon" type="image/png" href="/impactportal/favicon.ico"/>
    
    <script type="text/javascript">
    var c4i_https = "<%=Configuration.getHomeURLHTTPS()%>";
    </script>
    
    <link rel="stylesheet" href="/impactportal/js/jquery-ui-1.11.4/jquery-ui.min.css" />
    <script type="text/javascript" src="/impactportal/js/jquery-1.12.1.min.js"></script>
    <script type="text/javascript" src="/impactportal/js/fancybox/source/jquery.fancybox.js?v=2.1.4"></script>
    <link rel="stylesheet" type="text/css" href="/impactportal/js/fancybox/source/jquery.fancybox.css?v=2.1.4" media="screen" />
    <script type="text/javascript"           src="/impactportal/js/fancyboxinit.js"></script>
    <link rel="stylesheet" href="/impactportal/styles.css" type="text/css" />
    <script type="text/javascript" src="/impactportal/js/ImpactJS.js"></script>
	<script type="text/javascript" src="/impactportal/js/jquery-ui-1.11.4/jquery-ui.min.js"></script>
	<script type="text/javascript" src="/impactportal/js/jqueryextensions/jquery.dialogextend.min.js"></script>
	<script type="text/javascript" src="/impactportal/js/jqueryextensions/svg-pan-zoom.min.js"></script>