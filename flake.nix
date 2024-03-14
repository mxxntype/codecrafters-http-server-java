{
    description = "Java development environment";

    inputs = {
        flake-utils.url = "github:numtide/flake-utils";
        nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    };

    outputs = {
        self,
        flake-utils,
        nixpkgs
    }: flake-utils.lib.eachDefaultSystem (system:
        let
            pkgs = nixpkgs.legacyPackages.${system};
        in {
            devShells.default = with pkgs; mkShell {
                JDTLS_PATH = "${jdt-language-server}/share/java";
                packages = [
                    gradle
                    jdk
                    jdt-language-server
                ];
            };
        }
    );
}
