UiElementHandler = function() {

};

UiElementHandler.prototype.removeDivElement = function(id) {
	var divId = document.getElementById(id);
    if (!divId) {
        console.log("Can't remove divId " + id + " because it does not exist.");
    } else {
        divId.parentNode.removeChild(divId);
    }
};

UiElementHandler.prototype.setDivElementParent = function(id, parentId) {
	var divId = document.getElementById(id);
    var parentDiv = document.getElementById(parentId);
    parentDiv.appendChild(divId);
};

UiElementHandler.prototype.createDivElement = function(parentId, id, html, styleClass, clickAttribute) {
	var parent = document.getElementById(parentId);
	var index = parent.getElementsByTagName("*");
	var newdiv = document.createElement('div', [index]); 
	newdiv.setAttribute('id', id); 
	newdiv.setAttribute('onclick', clickAttribute); 
	newdiv.className = styleClass; 
	
	if (html) { 
			newdiv.innerHTML = html; 
		} 
		else { 
		newdiv.innerHTML = ""; 
		} 
	parent.appendChild(newdiv);
//	console.log(newdiv.id)
	return newdiv.id;
};


UiElementHandler.prototype.createTextInputElement = function(parentId, id, varName, styleClass) {
	var parent = document.getElementById(parentId);
	var index = parent.getElementsByTagName("*");
	var newdiv = document.createElement('input', [index]);

	newdiv.setAttribute('id', id); 	
	newdiv.setAttribute('type', "text"); 
	newdiv.setAttribute('name', varName); 
	
	newdiv.className = styleClass; 
//	console.log(varName);
		
	parent.appendChild(newdiv);
};


UiElementHandler.prototype.removeElements = function(release, elementId) {
	var opacity = release;
    var elementId = elementId;
	if (opacity > 0.999) {
		opacity = 0.95;
	}

	var removeInterval = setInterval(function() {
		if (document.getElementById(elementId) == null) {
			clearTimeout(removeInterval);
			return;
		}

		opacity = opacity * opacity;
		document.getElementById(elementId).style.opacity = opacity;
        document.getElementById(elementId).style.webkitTransform = "scale("+opacity+")";

		if (opacity < 0.05) {
			if ( document.getElementById(elementId).childNodes ) {
				    while ( document.getElementById(elementId).childNodes.length >= 1 )
				{
				    document.getElementById(elementId).removeChild( document.getElementById(elementId).firstChild );
				}
			}

			uiElementHandler.removeDivElement(elementId);
			clearInterval(removeInterval);
		}
			
	}, 50, this);
	
};

UiElementHandler.prototype.removeElementChildren = function(release, elementId) {
    var opacity = release;
    if (opacity > 0.99) {
        opacity = 0.95;
    }

    var removeInterval = setInterval(function() {
        if (document.getElementById(elementId) == null) {
            clearTimeout(removeInterval);
            return;
        }

        opacity = opacity * opacity;
    //    document.getElementById(elementId).style.opacity = opacity;

        if (opacity < 0.15) {
            if ( document.getElementById(elementId).childNodes ) {
                while ( document.getElementById(elementId).childNodes.length >= 1 )
                {
                    document.getElementById(elementId).removeChild( document.getElementById(elementId).firstChild );
                }
            }

            clearInterval(removeInterval);
        }

    }, 50, this);

};

UiElementHandler.prototype.createParentDivElement = function(id) {

    var elementData = {
        parent:{id:"viewContainer"},
        elementType:'div',
        attributes:{
            id:id
        }
    }
    var newdiv = this.createElement(elementData)

    var viewportMetaData = {
        parent:{tagName:"head"},
        elementType:'meta',
        attributes:{
            name:"viewport",
            id:"viewportMetaTag",
        }
    }

    var viewportMetaTag = this.createElement(viewportMetaData)
    this.setViewportDimensions();
    this.setOrientationChangeBehaviour();

    var homescreenMetaData = {
        parent:{tagName:"head"},
        elementType:'meta',
        attributes:{
            name:"apple-mobile-web-app-status-bar-style",
            content:"black-translucent"
        }
    }
    var homescreenMetaTag = this.createElement(homescreenMetaData)

    var webappMetaData = {
        parent:{tagName:"head"},
        elementType:'meta',
        attributes:{
            name:"apple-mobile-web-app-capable",
            content:"yes"
        }
    }
    var webappMetaTag = this.createElement(webappMetaData)
    return newdiv;
};


UiElementHandler.prototype.setViewportDimensions = function() {
    var viewportTagElement = document.getElementById("viewportMetaTag")
    var width = window.innerWidth;
    var height = window.innerHeight;

    var viewportWidth=540;

    if (width > height) {
        viewportWidth=1260;
    }

    viewportTagElement.setAttribute('content','width = '+width+'; user-scalable = no');
    console.log("Viewport width update to ---> w: "+width+" h:"+height);
}

UiElementHandler.prototype.setOrientationChangeBehaviour = function() {
    var instance = this;
    var setDimensions = function() {
        instance.setViewportDimensions()
    }

    window.addEventListener("orientationchange", function() {
        setDimensions()
    });
};

UiElementHandler.prototype.createElement = function(elementData) {
    var parentElement = null;
    var type = elementData.parent
    for (index in type) {
        if (index == "id") {
            parentElement = this.getElementById(type[index]);
        }
        if (index == "tagName") {
            parentElement = document.getElementsByTagName(type[index])[0];
        }
    }
    var index = parentElement.getElementsByTagName("*");
    var newElement = document.createElement(elementData.elementType, [index]);

    for (keys in elementData.attributes) {
        newElement.setAttribute(keys, elementData.attributes[keys])
    }
    parentElement.appendChild(newElement);
//    newElement.className = "OdoboBase"
    return newElement;
};