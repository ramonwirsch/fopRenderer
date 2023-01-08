<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format"
	xmlns:a="https://www.github.com/ramonwirsch">


	<xsl:output method="xml" indent="yes" />

	<xsl:template match="/">
		<xsl:apply-templates />
	</xsl:template>

	<xsl:template match="a:airport">
		<fo:root>
			<fo:layout-master-set>
				<fo:simple-page-master master-name="foo" page-height="10cm" page-width="10cm" margin="5mm">
					<fo:region-body />
				</fo:simple-page-master>
			</fo:layout-master-set>
			<fo:page-sequence master-reference="foo">

				<fo:flow flow-name="xsl-region-body">

					<fo:block>
						<xsl:value-of select="." />
					</fo:block>
				</fo:flow>
			</fo:page-sequence>

		</fo:root>
	</xsl:template>

</xsl:stylesheet>
