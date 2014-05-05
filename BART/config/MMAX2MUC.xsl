<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
                xmlns:mmax="org.eml.MMAX2.discourse.MMAX2DiscourseLoader"
                xmlns:response="www.eml.org/NameSpaces/response"
		xmlns:sentence="www.eml.org/NameSpaces/sentence">
<xsl:output method="xml" indent="no"/>

<!--
<xsl:template match="words">
<html>
 <body style="font-family:Arial;">
 <table>
  <xsl:apply-templates/>
 </table>
 </body>
</html>
</xsl:template>
-->

<xsl:template match="/">
  <DOC> 
    <xsl:apply-templates/>
  </DOC>
</xsl:template>

<xsl:template match="word">
 <xsl:text> </xsl:text>
 <xsl:apply-templates select="mmax:getStartedMarkables(@id)" mode="opening"/>
 <xsl:value-of select="text()"/>
 <xsl:apply-templates select="mmax:getEndedMarkables(@id)" mode="closing"/>
</xsl:template>

<!--
<xsl:template match="segment:markable" mode="opening">
<br/>
</xsl:template>

<xsl:template match="segment:markable" mode="closing">
</xsl:template>
-->

<!--
<xsl:template match="meta:markable" mode="opening">
 <xsl:text disable-output-escaping="yes">&lt;em&gt;</xsl:text>
</xsl:template>

<xsl:template match="meta:markable" mode="closing">
 <xsl:text disable-output-escaping="yes">&lt;/em&gt;</xsl:text>
</xsl:template>
-->

<xsl:template match="sentence:markable" mode="closing">
<xsl:text>
</xsl:text>
</xsl:template>

<xsl:template match="response:markable" mode="opening">
  <xsl:text disable-output-escaping="yes">&lt;MENTION ID=&quot;</xsl:text><xsl:value-of select="@id"/>
  <xsl:text disable-output-escaping="yes">&quot; EID=&quot;</xsl:text><xsl:value-of select="@coref_set"/>
  <xsl:text disable-output-escaping="yes">&quot; COS=&quot;</xsl:text><xsl:value-of select="@cos"/>
  <xsl:text disable-output-escaping="yes">&quot; COE=&quot;</xsl:text><xsl:value-of select="@coe"/>
  <xsl:text disable-output-escaping="yes">&quot; SPAN=&quot;</xsl:text><xsl:value-of select="@span"/>
  <xsl:text disable-output-escaping="yes">&quot; ETYPE=&quot;</xsl:text><xsl:value-of select="@etype"/>
  <xsl:text disable-output-escaping="yes">&quot; MTYPE=&quot;</xsl:text><xsl:value-of select="@mtype"/>
<xsl:text disable-output-escaping="yes">&quot;&gt;</xsl:text>
</xsl:template>

<xsl:template match="response:markable" mode="closing">
<xsl:text disable-output-escaping="yes">&lt;</xsl:text>/MENTION<xsl:text disable-output-escaping="yes">&gt;</xsl:text>
</xsl:template>

<!--
  <xsl:text disable-output-escaping="yes">&quot; >EID=<xsl:text disable-output-escaping="yes">&quot;</xsl:text><xsl:value-of select="@coref_set"/>
<xsl:template match="mmpos:markable" mode="closing">
 <xsl:if test="mmax:inMarkableFromLevel(@id,'pos_level','mcm_reference')">
 <sub> 
 <xsl:value-of select="@tag"/>
 </sub>
 </xsl:if>
</xsl:template>
-->
</xsl:stylesheet>
