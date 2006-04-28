<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:ts="http://www.xsume.com/Xutil/ToolSpecification">
	<xsl:output method="text" omit-xml-declaration="yes"/>
	<xsl:template match="/ts:toolspec/ts:usage">
		<xsl:choose>
			<xsl:when test="ts:form">
				<xsl:text>Command can take one of a number of forms:-</xsl:text>
				<xsl:for-each select="ts:form">
					<xsl:apply-templates select="."/>
				</xsl:for-each>
			</xsl:when>
			<xsl:otherwise>
				<xsl:apply-templates select="*"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<xsl:template match="ts:form">
		<xsl:text>

Form </xsl:text>
		<xsl:value-of select="@value"/>
		<xsl:text>...
</xsl:text>
		<xsl:apply-templates select="*"/>
	</xsl:template>
	<xsl:template match="ts:optionGroup[not(@ref)]">
		<xsl:apply-templates select="ts:option"/>
	</xsl:template>
	<xsl:template match="ts:optionGroup[@ref]">
		<xsl:variable name="foo" select="@ref"/>
		<xsl:apply-templates select="//ts:optionGroup[@id=$foo]"/>
	</xsl:template>
	<xsl:template match="ts:option">
		<xsl:choose>
			<xsl:when test="not(@type='hidden')">
				<xsl:if test="@minOccurs=0">
					<xsl:text>[ </xsl:text>
				</xsl:if>
				<xsl:text>-</xsl:text>
				<xsl:value-of select="ts:switch"/>
				<xsl:if test="ts:associatedArgument[@placement='afterSpace']">
					<xsl:text> </xsl:text>
				</xsl:if>
				<xsl:if test="ts:associatedArgument">
					<xsl:choose>
						<xsl:when test="ts:associatedArgument/ts:annotation">
							<xsl:text>&lt;</xsl:text>
							<xsl:value-of select="ts:associatedArgument/ts:annotation"/>
							<xsl:text>&gt;</xsl:text>
						</xsl:when>
						<xsl:otherwise>
							<xsl:text>&lt;</xsl:text>
							<xsl:value-of select="@id"/>
							<xsl:text>&gt;</xsl:text>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:if>
				<xsl:if test="@minOccurs=0">
					<xsl:text> ]</xsl:text>
				</xsl:if>
				<xsl:if test="(@maxOccurs='unbounded') or (@maxOccurs &gt; 1)">
					      <xsl:text>*</xsl:text>
	                        </xsl:if>
			    <!--xsl:text>&#10;</xsl:text-->
		            <xsl:choose>
		            <xsl:when xml:space="preserve" test="ts:annotation">				 
	                    <xsl:value-of xml:space="preserve" select="ts:annotation"/>
			    </xsl:when>
			    <xsl:otherwise>
			    <xsl:value-of select="@id"/>
			    </xsl:otherwise>
			    </xsl:choose>
			    <xsl:text>&#10;</xsl:text>
			    
			</xsl:when>
		</xsl:choose>
	</xsl:template>
	<xsl:template match="ts:argument">
		<xsl:choose>
			<xsl:when test="not(@type='hidden')">
				<xsl:if test="@minOccurs=0">
					<xsl:text>[ </xsl:text>
				</xsl:if>
				<xsl:text>&lt;</xsl:text>
				<xsl:value-of select="@id"/>
				<xsl:text>&gt;</xsl:text>
				<xsl:if test="@minOccurs=0">
					<xsl:text> ]</xsl:text>
				</xsl:if>
				<xsl:text>&#10;</xsl:text>
				<xsl:choose>
			    <xsl:when xml:space="preserve" test="ts:annotation">
			    <xsl:value-of xml:space="preserve" select="ts:annotation"/>
			     </xsl:when>
			     <xsl:otherwise>	
			     </xsl:otherwise>
			     </xsl:choose>
				<xsl:text>&#10;</xsl:text>
			</xsl:when>
		</xsl:choose>
	</xsl:template>
	<xsl:template match="@*|text()"/>
</xsl:stylesheet>
