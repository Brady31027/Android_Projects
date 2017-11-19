import { NgModule } from '@angular/core';
import { IonicPageModule } from 'ionic-angular';
import { AddShoppingItemPage } from './add-shopping-item';

@NgModule({
  declarations: [
    AddShoppingItemPage,
  ],
  imports: [
    IonicPageModule.forChild(AddShoppingItemPage),
  ],
})
export class AddShoppingItemPageModule {}
