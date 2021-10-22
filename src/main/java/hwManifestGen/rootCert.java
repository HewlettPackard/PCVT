//**********************************************************************
// (C) Copyright 2020-2021 Hewlett Packard Enterprise Development LP
// 
// Permission is hereby granted, free of charge, to any person obtaining a
// copy of this software and associated documentation files (the "Software"),
// to deal in the Software without restriction, including without limitation
// the rights to use, copy, modify, merge, publish, distribute, sublicense,
// and/or sell copies of the Software, and to permit persons to whom the
// Software is furnished to do so, subject to the following conditions:
// 
// The above copyright notice and this permission notice shall be included
// in all copies or substantial portions of the Software.
// 
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL
// THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR
// OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
// ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
// OTHER DEALINGS IN THE SOFTWARE.
//**********************************************************************

package hwManifestGen;

public class rootCert {
	private String rootCert;
	
	public rootCert() {
		populateRootCert_PRO(); // default choice for factory production PILOT
	}
	
	public String getRootCert() {
		return rootCert;
	}
	
	private void populateRootCert_PRO() {
		rootCert = "-----BEGIN CERTIFICATE-----\r\n" +
				"MIIFEDCCA3igAwIBAgIJAL2E0VzhhM94MA0GCSqGSIb3DQEBDAUAMIGUMQswCQYD\r\n" +
				"VQQGEwJVUzEOMAwGA1UECAwFVGV4YXMxLzAtBgNVBAoMJkhld2xldHQgUGFja2Fy\r\n" +
				"ZCBFbnRlcnByaXNlIERldmVsb3BtZW50MRgwFgYDVQQLDA9Db21wdXRlIERldmlj\r\n" +
				"ZXMxKjAoBgNVBAMMIUhQRSBEZXZpY2UgSWRlbnRpdHkgUm9vdCBDQSBBMDAwMTAe\r\n" +
				"Fw0yMTA0MTUwNDM1MzBaFw00NjA0MDkwNDM1MzBaMIGUMQswCQYDVQQGEwJVUzEO\r\n" +
				"MAwGA1UECAwFVGV4YXMxLzAtBgNVBAoMJkhld2xldHQgUGFja2FyZCBFbnRlcnBy\r\n" +
				"aXNlIERldmVsb3BtZW50MRgwFgYDVQQLDA9Db21wdXRlIERldmljZXMxKjAoBgNV\r\n" +
				"BAMMIUhQRSBEZXZpY2UgSWRlbnRpdHkgUm9vdCBDQSBBMDAwMTCCAaIwDQYJKoZI\r\n" +
				"hvcNAQEBBQADggGPADCCAYoCggGBAOdFatX9n3+GB9LJOGnZuYSWlbMpTZErldAO\r\n" +
				"sK16JhrQLYz3LWjsQ3KFOdm3AM189AR1373bSqXYnGMCosO9ueYDsiQB1dHUv00j\r\n" +
				"jw01m6oEcM9nbsjUxlzUeDbVgcvkzo34tbpoxlKfupGLJu9riq2AG6Y1dCPp/5+p\r\n" +
				"lLuRkLSY8wJvvngXGMtxDTBC7ZRRvCAzDwFx/5wOv90xwcmsrIYmmtuwiWJL3LdT\r\n" +
				"LCA/6cKtZ8zceoTUXSFcCD8I24YtqvH04b73IWjdO9dXCf6R2+V1mpDG35XxYFEi\r\n" +
				"+4C+xn17bm0HvKyhNK4KWKaPJQLaEcPRKhD11k3I+Tn18geXw12nzzoZF5XIr6jU\r\n" +
				"uetDSmsR+ZjUc5GERuhdhqjCPCIg/HREg8GIMxCqzjo/1n3kseb1UEFpylCmFol7\r\n" +
				"YobXPAvE4LzBDdL/h/7T4ZcSC1IDUOn9okKqPhA7oS04NySs+kG3i2oWumZ5ATJZ\r\n" +
				"50c1/HIbXB0e7juMFCTh3WD+Jt5GvwIDAQABo2MwYTAdBgNVHQ4EFgQUZ3ODEQ8U\r\n" +
				"/DEFlPw8dpe4KalB7HMwHwYDVR0jBBgwFoAUZ3ODEQ8U/DEFlPw8dpe4KalB7HMw\r\n" +
				"DwYDVR0TAQH/BAUwAwEB/zAOBgNVHQ8BAf8EBAMCAQYwDQYJKoZIhvcNAQEMBQAD\r\n" +
				"ggGBAMcyDBjsj/x09YSTzr7EyoOxqQg6CkrRYkVS0vx194TG9Qe25TEY+UGWqEDY\r\n" +
				"VrNX9oR5veTtbD6MATtNJh6ztQYf24BbR/otOWAC6q72U0/bxRgI0ZJc86Tua8lc\r\n" +
				"jls8y3aDc2ZAaEy12n5Dp8TdMgs7conM9huteD0letsSQLkYKloZMGp2LiWkR7TC\r\n" +
				"YdZTfWnGf0yx2sIX6+oQg3TVAvVV+hpJaikB38UVjGdB8iQ0gkeO/MBGvp8NWAlw\r\n" +
				"XQAoToHtfYWFcPuUnEXjkC6Uom4N8yGF78BJ5eGGWmBgYEpNevyS5+JJ2FUfZe76\r\n" +
				"7xFF39RYnLzCiN9X796wAkfNNAuTyT5pR5w8RH+qxGkoGgXtj54+wotuk9Yd+X59\r\n" +
				"UudntalhY/63HqV+eh9OYhoyN1LLJMVqh3Gq7nzgchyr2OGWpWofqvkxwZOsi9Z3\r\n" +
				"5chjXDxGJHjE0U+322SvEwjjLnOUrdwiu/UwR0gkmOaHSAoqHkG7ERS79EJ+MHFF\r\n" +
				"hnPmlg==\r\n" +
				"-----END CERTIFICATE-----";
	}
	
}
