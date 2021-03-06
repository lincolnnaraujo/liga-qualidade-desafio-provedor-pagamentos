package br.com.zup.edu.ligaqualidade.desafioprovadorpagamentos.modifique;

import br.com.zup.edu.ligaqualidade.desafioprovadorpagamentos.pronto.MetodoPagamento;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Solucao {

	/**
	 * 
	 * @param infoTransacoes dados das transações. A String está formatada da seguinte maneira:		
		<b>"valor,metodoPagamento,numeroCartao,nomeCartao,validade,cvv,idTransacao"</b>
		<ol>
		 <li> Valor é um decimal</li>
	 	 <li> O método de pagamento é 'DEBITO' ou 'CREDITO' </li>
	 	 <li> Validade é uma data no formato dd/MM/yyyy. </li>
	 	</ol>
	 	
	 * @param infoAdiantamentos informacao da transacao que pode ser recebida adiantada. A String está formatada da seguinte maneira:		
		<b>"idTransacao,taxa"</b>
		<ol>
	 	 <li> Taxa é um decimal </li>	 	 
	 	</ol> 
	 * 
	 * @return Uma lista de array de string com as informações na seguinte ordem:
	 * [status,valorOriginal,valorASerRecebidoDeFato,dataEsperadoRecebimento].
	 * <ol>
	 *  <li>O status pode ser 'pago' ou 'aguardando_pagamento'</li>
	 *  <li>O valor original e o a ser recebido de fato devem vir no formato decimal. Ex: 50.45</li>
	 *  <li>dataEsperadoRecebimento deve ser formatada como dd/MM/yyyy. Confira a classe {@link DateTimeFormatter}</li> 
	 * </ol> 
	 * 
	 * É esperado que o retorno respeite a ordem de recebimento
	 */
	public static List<String[]> executa(List<String> infoTransacoes, List<String> infoAdiantamentos) {

		List<String[]> respostaTransacoes = new ArrayList<>();

		infoTransacoes.forEach(transacao -> {
			String[] tran = transacao.split(",");
			String[] retornoTran =  new String[4];

			retornoTran[1] = tran[0];
			String idTran = tran[6];

			trataTransacao(tran, retornoTran);

			if (!infoAdiantamentos.isEmpty()){
				for (String adiantamento : infoAdiantamentos) {
					String[] tranAdiantamento = adiantamento.split(",");
					if (idTran.equals(tranAdiantamento[0])) {
						String percentualAdiantamentoTran = String.valueOf(Math.round(Double.parseDouble(tranAdiantamento[1]) * 100));
						trataAdiantamento(tran, percentualAdiantamentoTran, retornoTran);
					}
				}
			}

			respostaTransacoes.add(retornoTran);
		});

		return respostaTransacoes;
	}

	private static void trataTransacao(String[] tran, String[] retornoTran) {
		if (tran[1].equals(MetodoPagamento.DEBITO.toString())) {
			retornoTran[0] = "pago";
			retornoTran[2] = calculoTaxa(tran[0],3);
			retornoTran[3] = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
		} else {
			retornoTran[0] = "aguardando_pagamento";
			retornoTran[2] = calculoTaxa(tran[0],5);
			retornoTran[3] = LocalDate.now().plusDays(30).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
		}
	}

	private static void trataAdiantamento(String[] tran, String percentualAdiantamento, String[] retornoTran){
		//[status,valorOriginal,valorASerRecebidoDeFato,dataEsperadoRecebimento]
		retornoTran[0] = "pago";
		retornoTran[2] = calculoTaxa(retornoTran[2], Integer.parseInt(percentualAdiantamento));
		retornoTran[3] = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
	}

	private static String calculoTaxa(String valorTransacao, int porcentagem) {

		Double valor = Double.parseDouble(valorTransacao);
		valor = valor - (valor*(porcentagem/100.f));
		return BigDecimal.valueOf(valor).setScale(2, RoundingMode.HALF_EVEN).toString();

	}

}
