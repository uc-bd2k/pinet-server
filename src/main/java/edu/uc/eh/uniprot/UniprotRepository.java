package edu.uc.eh.uniprot;

import org.springframework.data.repository.CrudRepository;

// This will be AUTO IMPLEMENTED by Spring into a Bean called ProteinCRUDRepository
// CRUD refers Create, Read, Update, Delete

public interface UniprotRepository extends CrudRepository<Uniprot, Integer> {

    //private final UniprotService uniprotService;

//    private final FastaService fastaService;
//
//    @Autowired
//    public UniprotRepository(UniprotService uniprotService, FastaService fastaService)
//    {
//        this.fastaService = fastaService;
//        //this.uniprotService = uniprotService;
//    }
    //private final FastaService fastaService;

//    @Autowired
//    UniprotService uniprotService = new UniprotService();
//    private final FastaService fastaService;
//    @Autowired
//    public UniprotRepository(FastaService fastaService)
//    {
//        this.fastaService = fastaService;
//    }

    public Uniprot findByName(String name);


    //Don't use this method now!
//    public default JSONObject findByAccessionApi(String accession)
//    {
//        Uniprot response = new Uniprot();
//        JSONObject responseUniprot = new JSONObject();
//        String[] canonicalAccessionList = accession.split("-");
//        String organism = "";
//
//        String canonicalAccession = canonicalAccessionList[0];
//        try {
//
//
//            response = findByAccession(canonicalAccession);
//            responseUniprot = response.toJson();
//
//            //System.out.println(responseUniprot.toString());
//
//        } catch (Exception e) {
//
//
//            String msg =  String.format("Uniprot %s not found in uniprot localdb", accession);
//            System.out.println(msg);
//
//            //throw new RuntimeException(msg);
//            try {
//                responseUniprot =  uniprotService.getTable(canonicalAccession);
//                System.out.println(msg);
//
//            }
//            catch(Exception e2){
//                String msg2 =  String.format("Uniprot %s not found  in uniprot ", accession);
//                System.out.println(msg2);
//
//            }
////        response = new Uniprot();
//
//        }
//
//        if (canonicalAccession != accession)
//        {
//            try {
//                System.out.println(accession);
//                JSONObject fastaResult = fastaService.getTable(accession);
//                String fastaSeq = (String) fastaResult.get("sequence");
//                responseUniprot.remove("sequence");
//                responseUniprot.put("sequence", fastaSeq);
//                responseUniprot.remove("length");
//                responseUniprot.put("length", fastaSeq.length());
//
//            }
//            catch (Exception e2)
//            {
//                String msg =  String.format("Uniprot %s error in fasta", accession);
//                System.out.println(msg);
//            }
//        }
//
//
//
//
////        Uniprot protein = findByAccession(accession);
//        return responseUniprot;
//
//    }

    public Uniprot findByAccession(String accession);
}
