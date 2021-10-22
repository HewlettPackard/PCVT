package validator;

import com.beust.jcommander.Parameter;

import cli.pv.FileExistsParameterValidator;
import cli.pv.ReadFileParameterValidator;

public class ValidatorArgs {

    @Parameter(required=false, names={"-cchain", "--publicKeyCert"}, order=0, description="the public key certificate of the signing key", validateWith={FileExistsParameterValidator.class, ReadFileParameterValidator.class})
    private String publicKeyCert;
    
    @Parameter(required=true, names={"-spc", "--x509v2AttrCert"}, order=1, description="the certificate containing a signature to validate", validateWith={FileExistsParameterValidator.class, ReadFileParameterValidator.class})
    private String x509v2AttrCert;

    @Parameter(required=true, names={"-hwmanif", "--hwManifest"}, order=2, description="the Hardware Manifest (Paccor like) to be validated against the Platform Certficate Content", validateWith={FileExistsParameterValidator.class, ReadFileParameterValidator.class})
    private String hwManifest;

    @Parameter(names={"-h", "--help"}, order=3, help = true, description="print this help message")
    private boolean help;

    public String getPublicKeyCert() {
        return publicKeyCert;
    }

    public String getX509v2AttrCert() {
        return x509v2AttrCert;
    }

    public String getHwManifest() {
        return hwManifest;
    }

    public boolean isHelp() {
        return help;
    }
}
