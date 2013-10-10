var overview={
	xtype:'button',
	text: 'Show WPS overview',
	handler: function() { 
		
			container.removeAll();
			container.getLoader().load({url: 'js/components/processors/wpsoverview.js',renderer: 'component'} ) 
			
	}
}