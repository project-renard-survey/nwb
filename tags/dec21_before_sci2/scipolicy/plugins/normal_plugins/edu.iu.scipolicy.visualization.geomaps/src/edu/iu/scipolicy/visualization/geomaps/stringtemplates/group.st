group geomaps;




utilityDefinitions() ::= <<

% Pops two numbers
% Pushes the lesser of them
/2min { 2 copy ge { exch } if pop } bind def

% Pops two numbers
% Pushes the greater of them
/2max { 2 copy le { exch } if pop } bind def

% Pops n, the number of values to pop for averaging, and n values
% Pushes the mean of those n values
/mean {
	% Basically, fold + from 0 on the n values.
	% The final exch div divides the sum by n,
	% which we've kept rolled at the bottom of the stack.
	0 1 index {3 -1 roll add} repeat exch div
} def

% <string> underlinedShow
% Pops a string
% Shows the string underlined
/underlinedShowDict 1 dict def
/underlinedShow {
	underlinedShowDict begin
		/UNDERLINE_THICKNESS 0.1 def
		/UNDERLINE_VERTICAL_OFFSET -0.8 def

		gsave
			0 UNDERLINE_VERTICAL_OFFSET rmoveto
			dup stringwidth rlineto
			UNDERLINE_THICKNESS setlinewidth stroke
		grestore
		
		show
	end
} bind def

>>




circlePrinterDefinitions() ::= <<

% Pops x, y, r
% Pushes a circular path at (x,y) with radius r
/circle {
  newpath
    0 360 arc
  closepath
} def

>>




showToolNameDefinitions() ::= <<

% Pops x, y, text, font, fontBrightness, fontSize
% Shows text superscripted at (x, y)
% Pushes the point after the shown text (at the original/unsuperscripted y-level)
/showSuperscriptedDict 1 dict def
/showSuperscripted {
	showSuperscriptedDict begin
		/fontSize exch def
		/fontBrightness exch def
		/font exch def
		/text exch def
		/y exch def
		/x exch def

		gsave
			x y moveto
			
			% Show text raised by ((1/3) fontSize) and scaled by (3/5)
			gsave
				/upShift {1 3 div fontSize mul} bind def
				0 upShift rmoveto
				
				/relativeFontSize {3 5 div fontSize mul} bind def
				font relativeFontSize scalefont setfont	
				fontBrightness setgray
				
				text show
			grestore
			
			% Move to the point at the original baseline (y) directly after the shown text
			/textWidth {text stringwidth pop} bind def
			textWidth 0 rmoveto
			currentpoint
		grestore
	end
} def

% Pops x, y, font, fontBrightness, fontSize
% Shows "Sci^{2} Tool" at (x,y)
% Pushes the point at the end of the shown text
/showToolNameDict 1 dict def
/showToolName {
	showToolNameDict begin
		/fontSize exch def
		/fontBrightness exch def
		/font exch def
		/y exch def
		/x exch def

		gsave
			x y moveto
		
			font fontSize scalefont setfont		
			fontBrightness setgray
			
			
			(Sci) show			
			currentpoint (2) font fontBrightness fontSize showSuperscripted moveto			
			( Tool) show
			
			currentpoint
		grestore
	end
} def

>>




legendDefinitions() ::= <<

/legendLabelDict 1 dict def
/legendLabel {
	legendLabelDict begin
		/font exch def
		
		/keyLabelFontSize exch def
		/keyLabelBrightness exch def
		/keyLabel exch def
		
		/scalingLabelBrightness exch def
		/scalingLabel exch def
		
		/typeLabelFontSize exch def
		/typeLabelBrightness exch def
		/typeLabel exch def
		
		/y exch def
		/x exch def


		gsave
			x y moveto

			gsave
				font typeLabelFontSize scalefont setfont

				typeLabelBrightness setgray
				typeLabel show
				( ) show

				scalingLabelBrightness setgray
				scalingLabel show
			grestore
			
			gsave
				0 typeLabelFontSize neg rmoveto
				0 5 neg rmoveto
			
				font keyLabelFontSize scalefont setfont
				keyLabelBrightness setgray
				keyLabel show
			grestore
		grestore
	end
} def

>>


circleAreaLegend(x,y,minRadius,midRadius,maxRadius,circleBrightness,minLabel,midLabel,maxLabel,extremaLabelBrightness,extremaLabelFontSize,typeLabel,typeLabelBrightness,typeLabelFontSize,scalingLabel,scalingLabelBrightness,keyLabel,keyLabelBrightness,keyLabelFontSize,fontName) ::= <<

<x> <y> <minRadius> <midRadius> <maxRadius> <circleBrightness> (<minLabel>) (<midLabel>) (<maxLabel>) <extremaLabelBrightness> <extremaLabelFontSize> (<typeLabel>) <typeLabelBrightness> <typeLabelFontSize> (<scalingLabel>) <scalingLabelBrightness> (<keyLabel>) <keyLabelBrightness> <keyLabelFontSize> {/<fontName> findfont} circleAreaLegend

>>

circleAreaLegendDefinitions() ::= <<

/circleAreaLegendDict 1 dict def
/circleAreaLegend {
	circleAreaLegendDict begin
		/font exch def
		
		/keyLabelFontSize exch def
		/keyLabelBrightness exch def
		/keyLabel exch def
		
		/scalingLabelBrightness exch def
		/scalingLabel exch def
		
		/typeLabelFontSize exch def
		/typeLabelBrightness exch def
		/typeLabel exch def
		
		/extremaLabelFontSize exch def
		/extremaLabelBrightness exch def		
		/maxLabel exch def
		/midLabel exch def
		/minLabel exch def
		
		/circleBrightness exch def
		/maxRadius exch def
		/midRadius exch def
		/minRadius exch def
		
		/y exch def
		/x exch def


		gsave
			x y translate
		
			gsave
				0 keyLabelFontSize 10 add neg translate
			
				font extremaLabelFontSize scalefont setfont
				
				/minLabelwidth {minLabel stringwidth pop} bind def
				/midLabelwidth {midLabel stringwidth pop} bind def
				/maxLabelwidth {maxLabel stringwidth pop} bind def

				/circleX {maxRadius} bind def
				/minCircleY {minRadius neg 5 sub} bind def
				/midCircleY {midRadius neg 5 sub} bind def
				/maxCircleY {maxRadius neg 5 sub} bind def
				
				/labelX {2 maxRadius mul 5 add} bind def
				/minLabelY {minCircleY extremaLabelFontSize 2 div sub} bind def
				% Try to put vertically position the mid and max labels
				% so that they center on their circle's center, but if that
				% would result in overlapping labels, position them yet lower
				% to prevent that.
				/midLabelY {midCircleY extremaLabelFontSize 2 div sub minLabelY extremaLabelFontSize sub 2min} bind def
				/maxLabelY {maxCircleY extremaLabelFontSize 2 div sub midLabelY extremaLabelFontSize sub 2min} bind def
				
				% Minimum
				%   Circle
				gsave
					circleBrightness setgray
					circleX minCircleY minRadius circle stroke
				grestore
				%   Label
				gsave
					extremaLabelBrightness setgray
					labelX minLabelY moveto
					minLabel show
				grestore

				% Midpoint
				%   Circle
				gsave
					circleBrightness setgray
					circleX midCircleY midRadius circle stroke
				grestore
				%   Label
				gsave
					extremaLabelBrightness setgray
					labelX midLabelY moveto
					midLabel show
				grestore
				
				% Maximum
				%   Circle
				gsave
					circleBrightness setgray
					circleX maxCircleY maxRadius circle stroke
				grestore
				%   Label
				gsave
					extremaLabelBrightness setgray
					labelX maxLabelY moveto
					maxLabel show
				grestore
			grestore

			0 0 typeLabel typeLabelBrightness typeLabelFontSize scalingLabel scalingLabelBrightness keyLabel keyLabelBrightness keyLabelFontSize font legendLabel
		grestore
	end
} def

>>



colorLegend(x,y,gradientWidth,gradientHeight,gradientResolution,minColorRed,minColorGreen,minColorBlue,maxColorRed,maxColorGreen,maxColorBlue,minLabel,midLabel,maxLabel,extremaLabelBrightness,extremaLabelFontSize,typeLabel,typeLabelBrightness,typeLabelFontSize,scalingLabel,scalingLabelBrightness,keyLabel,keyLabelBrightness,keyLabelFontSize,fontName) ::= <<

<x> <y> <gradientWidth> <gradientHeight> <gradientResolution> <minColorRed> <minColorGreen> <minColorBlue> <maxColorRed> <maxColorGreen> <maxColorBlue> (<minLabel>) (<midLabel>) (<maxLabel>) <extremaLabelBrightness> <extremaLabelFontSize> (<typeLabel>) <typeLabelBrightness> <typeLabelFontSize> (<scalingLabel>) <scalingLabelBrightness> (<keyLabel>) <keyLabelBrightness> <keyLabelFontSize> {/<fontName> findfont} colorLegend

>>

colorLegendDefinitions() ::= <<

% x y width height red green blue
% Draw counter-clockwise from lower-left the box whose
% lower-left corner is (x, y) having the given width and height.
/boxDict 1 dict def
/box {
	boxDict begin
		/height exch def
		/width exch def
		/y exch def
		/x exch def

		newpath
			x y moveto
			width 0 rlineto
			0 height rlineto
			width neg 0 rlineto
		closepath
	end
} def

% x xmin xmax ymin ymax
% Linear interpolation from xmin-to-xmax to ymin-to-ymax
/interpolateDict 1 dict def
/interpolate {
	interpolateDict begin
		/ymax exch def
		/ymin exch def
		/xmax exch def
		/xmin exch def
		/x exch def

		ymax ymin sub xmax xmin sub div x xmin sub mul ymin add
	end
} def

% x y gradientWidth gradientHeight minColorRed minColorGreen minColorBlue maxColorRed maxColorGreen maxColorBlue
% Draw a gradient from lowcolor to highcolor with lower-left corner (x, y)
% having the given width and height.
/colorLegendDict 1 dict def
/colorLegend {
	colorLegendDict begin
		/font exch def
		
		/keyLabelFontSize exch def
		/keyLabelBrightness exch def
		/keyLabel exch def
		
		/scalingLabelBrightness exch def
		/scalingLabel exch def
		
		/typeLabelFontSize exch def
		/typeLabelBrightness exch def
		/typeLabel exch def
		
		/extremaLabelFontSize exch def
		/extremaLabelBrightness exch def		
		/maxLabel exch def
		/midLabel exch def
		/minLabel exch def
		
		/maxColorBlue exch def
		/maxColorGreen exch def
		/maxColorRed exch def
		
		/minColorBlue exch def
		/minColorGreen exch def
		/minColorRed exch def
		
		/gradientResolution exch def
		/gradientHeight exch def
		/gradientWidth exch def
		
		/y exch def
		/x exch def
		
		
		/tickHeight {3} bind def

		gsave
			x y translate
		
			% Draw the color gradient/spectrum
			gsave
				0 keyLabelFontSize 10 add neg translate
			
				/firstGradientX {0} bind def
				/lastGradientX {firstGradientX gradientWidth add gradientResolution sub} bind def
				/gradientY {0 gradientHeight sub 5 sub} bind def
			
				firstGradientX gradientResolution lastGradientX {
					/ii exch def
					
					% Draw black ticks below the first, middle, and last cells
					/isFirstCell {ii firstGradientX sub abs gradientResolution lt} bind def
					/isMiddleCell {ii lastGradientX firstGradientX 2 mean round sub abs gradientResolution 2 div lt} bind def
					/isLastCell {ii lastGradientX sub abs gradientResolution lt} bind def
					
					isFirstCell isMiddleCell or isLastCell or {
						gsave
							ii gradientY tickHeight sub gradientResolution tickHeight box
							0 setgray
							fill
						grestore
					} if
					
					% Draw the ii'th colored cell of the gradient
					/red {ii firstGradientX lastGradientX minColorRed maxColorRed interpolate} bind def
					/green {ii firstGradientX lastGradientX minColorGreen maxColorGreen interpolate} bind def
					/blue {ii firstGradientX lastGradientX minColorBlue maxColorBlue interpolate} bind def
					red green blue setrgbcolor
					ii gradientY gradientResolution gradientHeight box
					fill
				} for
			grestore

			% Draw the extrema labels (min, mid, max)
			gsave
				0 keyLabelFontSize 10 add neg translate
			
				font extremaLabelFontSize scalefont setfont
				extremaLabelBrightness setgray
				
				/minLabelwidth {minLabel stringwidth pop} bind def
				/midLabelwidth {midLabel stringwidth pop} bind def
				/maxLabelwidth {maxLabel stringwidth pop} bind def
				
				/minLabelX {0} bind def
				/midLabelX {gradientWidth 2 div midLabelwidth 2 div sub} bind def
				/maxLabelX {gradientWidth maxLabelwidth sub} bind def
				
				/labelY {0 5 sub gradientHeight sub 5 sub extremaLabelFontSize sub} bind def

				minLabelX labelY moveto
				minLabel show
				
				midLabelX labelY moveto
				midLabel show
				
				maxLabelX labelY moveto
				maxLabel show
			grestore

			% Draw the type and key labels
			0 0 typeLabel typeLabelBrightness typeLabelFontSize scalingLabel scalingLabelBrightness keyLabel keyLabelBrightness keyLabelFontSize font legendLabel
		grestore
	end
} def

>>