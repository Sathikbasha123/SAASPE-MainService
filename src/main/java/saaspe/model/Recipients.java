package saaspe.model;

import java.util.ArrayList;

import lombok.Data;

@Data
public class Recipients {

	private ArrayList<Signer> signers;
	private ArrayList<Carboncopy> cc;
}
