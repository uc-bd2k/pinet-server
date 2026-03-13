package edu.uc.eh.uniprot;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Entity // This tells Hibernate to make a table with name Uniprot out of this class
public class Uniprot {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)


    private Integer id;
    private String family;
    @Size(max=3000)
    private String sequence;
    private String subcellular_location;
    private String accession;
    private String interactant;
    private String go;
    private String nucleotide_phosphate_binding_region;
    private String synonym_gene_names;
    private String active_sites;
    private String length_seq;
    private String tissue_specificity;
    private String drug_bank;
    private String binding_sites;
    private String primary_gene_name;
    private String function;
    private String kegg;
    private String binding_db;
    private String string_id;
    private String chembl;
    private String accession_all;
    private String ensembl;
    private String full_name;
    private String sub_family;
    private String activity_regulation;
    private String superfamily;
    private String name;
    private String ucsc;
    private String gene_id;
    private String domains;
    private String reactome;
    private String organism;
    private String hgnc;





//    private String name;
//
//    private String email;

//


//			family=family&sequence=sequence&subcellular_location=subcellular_location&accession=accession2&interactant=interactant&go=go&nucleotide_phosphate_binding_region=nucleotide_phosphate_binding_region&synonym_gene_names=synonym_gene_names&active_sites=active_sites&length_seq=length_seq&tissue_specificity=tissue_specificity&drug_bank=drug_bank&binding_sites=binding_sites&primary_gene_name=primary_gene_name&function_in_cell=function_in_cell&kegg=kegg&binding_db=binding_db&string_id=string_id&chembl=chembl&accession_all=accession_all&ensembl=ensembl&full_name=full_name&sub_family=sub_family&activity_regulation=activity_regulation&superfamily=superfamily&name=name1&ucsc=ucsc&gene_id=gene_id&domains=domains&reactome=reactome&organism=organism&hgnc=hgnc
//family=family&sequence=sequence&subcellular_location=subcellular_location&accession=accession3&interactant=interactant&go=go&nucleotide_phosphate_binding_region=nucleotide_phosphate_binding_region&synonym_gene_names=synonym_gene_names&active_sites=active_sites&length_seq=length_seq&tissue_specificity=tissue_specificity&drug_bank=drug_bank&binding_sites=binding_sites&primary_gene_name=primary_gene_name&function_in_cell=function_in_cell&kegg=kegg&binding_db=binding_db&string_id=string_id&chembl=chembl&accession_all=accession_all&ensembl=ensembl&full_name=full_name&sub_family=sub_family&activity_regulation=activity_regulation&superfamily=superfamily&name=name2&ucsc=ucsc&gene_id=gene_id&domains=domains&reactome=reactome&organism=organism&hgnc=hgnc
//family=family&sequence=sequence&subcellular_location=subcellular_location&accession=accession4&interactant=interactant&go=go&nucleotide_phosphate_binding_region=nucleotide_phosphate_binding_region&synonym_gene_names=synonym_gene_names&active_sites=active_sites&length_seq=length_seq&tissue_specificity=tissue_specificity&drug_bank=drug_bank&binding_sites=binding_sites&primary_gene_name=primary_gene_name&function_in_cell=function_in_cell&kegg=kegg&binding_db=binding_db&string_id=string_id&chembl=chembl&accession_all=accession_all&ensembl=ensembl&full_name=full_name&sub_family=sub_family&activity_regulation=activity_regulation&superfamily=superfamily&name=name3&ucsc=ucsc&gene_id=gene_id&domains=domains&reactome=reactome&organism=organism&hgnc=hgnc
//family=family&sequence=sequence&subcellular_location=subcellular_location&accession=accession5&interactant=interactant&go=go&nucleotide_phosphate_binding_region=nucleotide_phosphate_binding_region&synonym_gene_names=synonym_gene_names&active_sites=active_sites&length_seq=length_seq&tissue_specificity=tissue_specificity&drug_bank=drug_bank&binding_sites=binding_sites&primary_gene_name=primary_gene_name&function_in_cell=function_in_cell&kegg=kegg&binding_db=binding_db&string_id=string_id&chembl=chembl&accession_all=accession_all&ensembl=ensembl&full_name=full_name&sub_family=sub_family&activity_regulation=activity_regulation&superfamily=superfamily&name=name4&ucsc=ucsc&gene_id=gene_id&domains=domains&reactome=reactome&organism=organism&hgnc=hgnc



    @Override
    public String toString() {
        return "Protein{" +
                "id='" + id + '\'' +
                "family='" + family + '\'' +
                ", sequence='" + sequence + '\'' +
                ", subcellular_location='" + subcellular_location + '\'' +
                ", accession='" + accession + '\'' +
                ", interactant='" + interactant + '\'' +
                ", go='" + go + '\'' +
                ", nucleotide_phosphate_binding_region='" + nucleotide_phosphate_binding_region + '\'' +
                ", synonym_gene_names='" + synonym_gene_names + '\'' +
                ", active_sites='" + active_sites + '\'' +
                ", length_seq=" + length_seq +
                ", tissue_specificity='" + tissue_specificity + '\'' +
                ", drug_bank='" + drug_bank + '\'' +
                ", binding_sites='" + binding_sites + '\'' +
                ", primary_gene_name='" + primary_gene_name + '\'' +
                ", function='" + function + '\'' +
                ", kegg='" + kegg + '\'' +
                ", binding_db='" + binding_db + '\'' +
                ", string_id='" + string_id + '\'' +
                ", chembl='" + chembl + '\'' +
                ", accession_all='" + accession_all + '\'' +
                ", ensembl='" + ensembl + '\'' +
                ", full_name='" + full_name + '\'' +
                ", sub_family='" + sub_family + '\'' +
                ", activity_regulation='" + activity_regulation + '\'' +
                ", superfamily='" + superfamily + '\'' +
                ", name='" + name + '\'' +
                ", ucsc='" + ucsc + '\'' +
                ", gene_id='" + gene_id + '\'' +
                ", domains='" + domains + '\'' +
                ", reactome='" + reactome + '\'' +
                ", organism='" + organism + '\'' +
                ", hgnc='" + hgnc + '\'' +
                '}';
    }

    public JSONObject toJson() {
        JSONObject protein_json = new JSONObject();

        List<String> binding_sites_list=new ArrayList();
        JSONArray reactome_json_list = new JSONArray();
        JSONArray go_json_list = new JSONArray();
        JSONObject reactome_json = new JSONObject();
        JSONObject go_json = new JSONObject();
        int i;
        //String[] binding_sites_list  = new String[0];
        if ( binding_sites != null ) {
            binding_sites_list = Arrays.asList(binding_sites.split("///"));
        }

        String[] nucleotide_phosphate_binding_region_list  = new String[0];
        if ( nucleotide_phosphate_binding_region != null ) {
            nucleotide_phosphate_binding_region_list =
                    nucleotide_phosphate_binding_region.split("///");
        }

        String[] domains_list  = new String[0];
        if ( domains != null ) {
            domains_list = domains.toString().split("///");
        }

        String[] subcellular_location_list = new String[0];
        if ( subcellular_location != null ) {
            subcellular_location_list = subcellular_location.toString().split("///");
        }

        String[] active_sites_list = new String[0];
        if ( active_sites != null ) {
            active_sites_list = active_sites.toString().split("///");
        }

        String[] synonym_gene_names_list = new String[0];
        if ( synonym_gene_names != null ) {
            synonym_gene_names_list = synonym_gene_names.toString().split("///");
        }


        String[] accession_all_list = new String[0];
        if ( accession_all != null ) {
            accession_all_list = accession_all.toString().split("///");
        }

        String[] interactant_list = new String[0];
        if ( interactant != null ) {
            interactant_list = interactant.toString().split("///");
        }

        String[] reactome_list = new String[0];
        if ( reactome != null ) {
            reactome_list = reactome.toString().split("///");
            for (String s: reactome_list) {
                String[] splitted = s.split("id:|/value:");
                reactome_json = new JSONObject();
                reactome_json.put("id", splitted[1]);
                reactome_json.put("value", splitted[2]);
                reactome_json_list.add(reactome_json);
            }
        }

        String[] go_list = new String[0];
        if ( go != null ) {
            go_list = go.toString().split("///");
            for (String s: go_list)
            {
                String[] splitted = s.split("id:|/value:");
                go_json = new JSONObject();
                go_json.put("id",splitted[1]);
                go_json.put("value",splitted[2]);
                go_json_list.add(go_json);

//                for (String s2: splitted)
//                {
//                    System.out.println(s2);
//                }
//                System.out.println("----------------");
                //s = s.split("\\value:");
            }
        }

        String[] ensembl_list = new String[0];
        if ( ensembl != null ) {
            ensembl_list = ensembl.toString().split("///");
        }

        List<String> primary_gene_name_list = new ArrayList();
        if ( primary_gene_name != null ) {
            primary_gene_name_list = new ArrayList<>(Arrays.asList(primary_gene_name.toString().split("///")));
        }

        List<String> function_list = new ArrayList();
        if ( function != null ) {
            function_list = new ArrayList<>(Arrays.asList(function.toString().split("///")));
        }



        protein_json.put("family",family);
        protein_json.put("sequence",sequence);
        protein_json.put("subcellular_location",subcellular_location_list);
        protein_json.put("accession",accession);
        protein_json.put("interactant", interactant_list);
        protein_json.put("go", go_json_list);
        protein_json.put("nucleotide_phosphate_binding_region", nucleotide_phosphate_binding_region_list);
        protein_json.put("synonym_gene_names", synonym_gene_names_list);
        protein_json.put("active_sites", active_sites_list);
        protein_json.put("length", length_seq);
        protein_json.put("tissue_specificity", tissue_specificity);
        protein_json.put("drug_bank", drug_bank);
        protein_json.put("binding_sites", binding_sites_list);
        protein_json.put("primary_gene_name", primary_gene_name_list);
        protein_json.put("function", function_list);
        protein_json.put("kegg", kegg);
        protein_json.put("binding_db", binding_db);
        protein_json.put("string_id", string_id);
        protein_json.put("chembl", chembl);
        protein_json.put("accession_all", accession_all_list);
        protein_json.put("ensembl", ensembl_list);
        protein_json.put("full_name", full_name);
        protein_json.put("sub_family", sub_family);
        protein_json.put("activity_regulation", activity_regulation);
        protein_json.put("superfamily", superfamily);
        protein_json.put("name", name);
        protein_json.put("ucsc", ucsc);
        protein_json.put("gene_id", gene_id);
        protein_json.put("domains", domains_list);
        protein_json.put("reactome", reactome_json_list);
        protein_json.put("organism", organism);
        protein_json.put("hgnc", hgnc);
//        System.out.println(protein_json.toString());
//        System.out.println("--------");
        return protein_json;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        this.family = family;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public String getSubcellular_location() {
        return subcellular_location;
    }

    public void setSubcellular_location(String subcellular_location) {
        this.subcellular_location = subcellular_location;
    }

    public String getAccession() {
        return accession;
    }

    public void setAccession(String accession) {
        this.accession = accession;
    }

    public String getInteractant() {
        return interactant;
    }

    public void setInteractant(String interactant) {
        this.interactant = interactant;
    }

    public String getGo() {
        return go;
    }

    public void setGo(String go) {
        this.go = go;
    }

    public String getNucleotide_phosphate_binding_region() {
        return nucleotide_phosphate_binding_region;
    }

    public void setNucleotide_phosphate_binding_region(String nucleotide_phosphate_binding_region) {
        this.nucleotide_phosphate_binding_region = nucleotide_phosphate_binding_region;
    }

    public String getSynonym_gene_names() {
        return synonym_gene_names;
    }

    public void setSynonym_gene_names(String synonym_gene_names) {
        this.synonym_gene_names = synonym_gene_names;
    }

    public String getActive_sites() {
        return active_sites;
    }

    public void setActive_sites(String active_sites) {
        this.active_sites = active_sites;
    }

    public String getLength_seq() {
        return length_seq;
    }

    public void setLength_seq(String  length_seq) {
        this.length_seq = length_seq;
    }

    public String getTissue_specificity() {
        return tissue_specificity;
    }

    public void setTissue_specificity(String tissue_specificity) {
        this.tissue_specificity = tissue_specificity;
    }

    public String getDrug_bank() {
        return drug_bank;
    }

    public void setDrug_bank(String drug_bank) {
        this.drug_bank = drug_bank;
    }

    public String getBinding_sites() {
        return binding_sites;
    }

    public void setBinding_sites(String binding_sites) {
        this.binding_sites = binding_sites;
    }

    public String getPrimary_gene_name() {
        return primary_gene_name;
    }

    public void setPrimary_gene_name(String primary_gene_name) {
        this.primary_gene_name = primary_gene_name;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public String getKegg() {
        return kegg;
    }

    public void setKegg(String kegg) {
        this.kegg = kegg;
    }

    public String getBinding_db() {
        return binding_db;
    }

    public void setBinding_db(String binding_db) {
        this.binding_db = binding_db;
    }

    public String getStringId() {
        return string_id;
    }

    public void setStringId(String string_id) {
        this.string_id = string_id;
    }

    public String getChembl() {
        return chembl;
    }

    public void setChembl(String chembl) {
        this.chembl = chembl;
    }

    public String getAccession_all() {
        return accession_all;
    }

    public void setAccession_all(String accession_all) {
        this.accession_all = accession_all;
    }

    public String getEnsembl() {
        return ensembl;
    }

    public void setEnsembl(String ensembl) {
        this.ensembl = ensembl;
    }

    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public String getSub_family() {
        return sub_family;
    }

    public void setSub_family(String sub_family) {
        this.sub_family = sub_family;
    }

    public String getActivity_regulation() {
        return activity_regulation;
    }

    public void setActivity_regulation(String activity_regulation) {
        this.activity_regulation = activity_regulation;
    }

    public String getSuperfamily() {
        return superfamily;
    }

    public void setSuperfamily(String superfamily) {
        this.superfamily = superfamily;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUcsc() {
        return ucsc;
    }

    public void setUcsc(String ucsc) {
        this.ucsc = ucsc;
    }

    public String getGene_id() {
        return gene_id;
    }

    public void setGene_id(String gene_id) {
        this.gene_id = gene_id;
    }

    public String getDomains() {
        return domains;
    }

    public void setDomains(String domains) {
        this.domains = domains;
    }

    public String getReactome() {
        return reactome;
    }

    public void setReactome(String reactome) {
        this.reactome = reactome;
    }

    public String getOrganism() {
        return organism;
    }

    public void setOrganism(String organism) {
        this.organism = organism;
    }

    public String getHgnc() {
        return hgnc;
    }

    public void setHgnc(String hgnc) {
        this.hgnc = hgnc;
    }
}
