<?xml version="1.0" encoding="UTF-8"?>

<!--
    Document   : generic.xsl
    Created on : 16. April 2008, 09:41
    Author     : versley
    Description:
        basic markup for chunked text
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="html"/>

    <!-- TODO customize transformation rules
         syntax recommendation http://www.w3.org/TR/xslt
    -->
    <xsl:template match="/">
        <text>
            <xsl:apply-templates/>
        </text>
    </xsl:template>
    <xsl:template match="sentence">
        <s><xsl:apply-templates/></s>
    </xsl:template>
    <xsl:template match="response">
        <coref>
            <xsl:if test="@coref_set!=''">
                <xsl:attribute name="set-id">
                    <xsl:value-of select="@coref_set"/>
                </xsl:attribute>
            </xsl:if>
    <xsl:apply-templates/>
     </coref>
    </xsl:template>
    <xsl:template match="pos">
    <w pos="{@tag}"><xsl:value-of select="word/@f"/></w>
    </xsl:template>
    <xsl:template match="text()"></xsl:template>
</xsl:stylesheet>
