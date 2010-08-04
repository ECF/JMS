<?xml version="1.0" encoding="UTF-8"?>
<md:mspec xmlns:md="http://www.eclipse.org/buckminster/MetaData-1.0" 
    name="org.eclipse.ecf.jms" 
    materializer="p2" 
    url="org.eclipse.ecf.jms.cquery">
    
    <md:mspecNode namePattern="^org\.eclipse\.ecf\.provider\.jms(\..+)?" materializer="workspace"/>
    <md:mspecNode namePattern="^org\.eclipse\.ecf\.provider\.jms.feature?" materializer="workspace"/>

    <md:mspecNode namePattern=".*" installLocation="${targetPlatformPath}"/>
</md:mspec>
	
