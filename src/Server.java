public class Server {
    public static void main(String[] args) {
        while(true){
            switch (recievedCommand){
                case "L":
                    getList();
                    break;
                case "N":
                    downloadFile():
                    break;
                case "U":
                    uploadFile();
                    break;
                case "D":
                    deleteFile():
                    break;
                case "R":
                    renameFile();
                    break;
            }
        }
    }
}
