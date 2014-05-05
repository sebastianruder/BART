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
        <div class="minidisc" id="coref">
            <xsl:apply-templates/>
        </div>
    </xsl:template>
    <xsl:template match="sentence">
        <xsl:apply-templates/>
        <br/>
    </xsl:template>
    <xsl:template match="chunk">
        [<sub class="chunk"><xsl:value-of select="@tag"/></sub>
        <xsl:apply-templates/>
        ]
    </xsl:template>
    <xsl:template match="enamex">
        [<sub class="chunk"><xsl:value-of select="@tag"/></sub>
        <xsl:apply-templates/>
        ]
    </xsl:template>
    <xsl:template match="markable">
        {<sub class="chunk"><xsl:value-of select="@label"/></sub>
        <xsl:apply-templates/>
        }
    </xsl:template>
    <xsl:template match="response">
        <span>
            <xsl:if test="@coref_set!=''">
                <xsl:attribute name="set-id">
                    <xsl:value-of select="@coref_set"/>
                </xsl:attribute>
            </xsl:if>
         <xsl:apply-templates/>
     </span>
    </xsl:template>
    <xsl:template match="pos">
        <xsl:value-of select="word/@f"/>
        <xsl:if test="not(contains('.,:',@tag))">
            <sub class="postag"><xsl:value-of select="@tag"/></sub>
        </xsl:if>
    </xsl:template>
    <xsl:template match="word">
        <xsl:value-of select="@f"/><xsl:text> </xsl:text>
    </xsl:template>
</xsl:stylesheet>
