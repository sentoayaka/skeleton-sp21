package gitlet;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */

    private static void validateArgs(String[] args, int expectedCount) {
        if (args.length != expectedCount) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        // TODO: what if args is empty?
        if(args.length == 0){
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        String firstArg = args[0];

        if (!firstArg.equals("init") && !Repository.GITLET_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }

        switch(firstArg) {
            case "init":
                validateArgs(args, 1);
                Repository.init();
                break;

            case "add":
                validateArgs(args, 2);
                Repository.add(args[1]);
                break;

            case "commit":
                validateArgs(args, 2);
                Repository.commit(args[1]);
                break;

            case "log":
                validateArgs(args, 1);
                Repository.log();
                break;

            case "checkout":
                if(args.length == 3 && args[1].equals("--")) {
                    Repository.checkoutFile(args[2]);
                } else if(args.length == 4 && args[2].equals("--")) {
                    Repository.checkoutCommitFile(args[1], args[3]);
                } else if(args.length == 2) {
                    Repository.checkoutBranch(args[1]);
                } else {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                break;

            case "rm":
                validateArgs(args, 2);
                Repository.remove(args[1]);
                break;

            case "status":
                validateArgs(args, 1);
                Repository.status();
                break;

            case "global-log":
                validateArgs(args, 1);
                Repository.globalLog();
                break;

            case "find":
                validateArgs(args, 2);
                Repository.find(args[1]);
                break;

            case "reset":
                validateArgs(args, 2);
                Repository.reset(args[1]);
                break;

            case "branch":
                validateArgs(args, 2);
                Repository.addBranch(args[1]);
                break;

            case "rm-branch":
                validateArgs(args, 2);
                Repository.removeBranch(args[1]);
                break;

            case "merge":
                validateArgs(args, 2);
                Repository.merge(args[1]);
                break;

            default:
                System.out.println("No command with that name exists.");
                System.exit(0);
        }
    }
}
