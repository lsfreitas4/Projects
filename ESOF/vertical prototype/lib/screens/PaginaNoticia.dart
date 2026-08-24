import 'dart:ui';

import 'package:flutter/material.dart';


class PaginaNoticia extends StatelessWidget {
  const PaginaNoticia({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
        title: 'Daily Feup',
        theme: ThemeData(
          scaffoldBackgroundColor: const Color.fromRGBO(0x8C, 0x2D, 0x19, 1),
        ),
        home: Scaffold(appBar: AppBar(
          title: const Text("DailyFeup"),
          backgroundColor: const Color.fromRGBO(0x8C, 0x2D, 0x19, 1)),
            body: ListView(

                children: <Widget>[
                  const Text( "FEUP assinala Dia Mundial da Saúde com Caminhada Solidária"
                      ,style: TextStyle(fontSize: 25.0 ,fontWeight:FontWeight.normal,color: Colors.white)),
                  Container(
                    margin: const EdgeInsets.all(20),
                    child: const Image(image: AssetImage("imgs/noticia1.jpg")),
                  ),

                  const Text( """Na Faculdade de Engenharia da Universidade do Porto (FEUP), o Dia Mundial da Saúde, que se assinala a 7 de abril, vai ser celebrado a caminhar de forma solidária. Pelas 12h15, com ponto de encontro marcado em frente à Biblioteca da FEUP, o destino é o novo Parque Central da Asprela e os seus mais de 60 mil metros quadrados ocupados por árvores, ribeiras e percursos pedonais e cicláveis que ligam as faculdades e centros de investigação do Polo da Asprela.
Após a caminhada até ao parque, haverá lugar para almoço e ainda uma atividade desportiva dinamizada por Marisa Sousa, monitora do programa Pausa Ativa do Centro de Desporto da Universidade do Porto (CDUP).
O valor da inscrição na Caminhada Solidária é de 10 euros e inclui uma t-shirt solidária e um almoço ligeiro. O dinheiro angariado vai reverter totalmente a favor de um fundo de emergência criado pelo Comissariado Social da FEUP e pela Associação de Estudantes da FEUP (AEFEUP) para estudantes com necessidades financeiras.
A iniciativa, dinamizada pelos Comissariados Social e Desportivo e pela AEFEUP, com o apoio do CDUP-UP e do Intercultural Contact Point (iPoint) da FEUP, vem reforçar o projeto “A Arte de Ajudar”. O seu propósito é promover a solidariedade no seio da comunidade académica e ajudar quem mais necessita através da venda de t-shirts solidárias, cujo design, criado por José Raimundo, estudante do Programa Doutoral em Media Digitais da FEUP, representa a entreajuda que se faz sentir no campus.
“Lançámos a iniciativa em 2021, numa altura em que a nossa comunidade estudantil assistia à maioria das aulas em casa e o campus FEUP estava despido de movimento e de vida. Com o retomar da atividade presencial, consideramos ser o momento certo para reavivar este projeto,”, explica Marisa Silva, membro do Comissariado Social da Faculdade de Engenharia e uma das responsáveis pela iniciativa.
Os membros da comunidade que já adquiriram a sua t-shirt solidária na venda do ano passado não necessitam de fazer este pagamento, mas devem inscrever-se e trazer vestida a t-shirt no momento da Caminhada Solidária.
Os interessados devem inscrever-se até ao dia 3 de abril.
Mais informações através do e-mail respsocial@fe.up.pt"""
                      ,style: TextStyle(fontSize: 20.0 ,fontWeight:FontWeight.normal,color: Colors.white))
              ]
            ),

            )
    );
  }
}