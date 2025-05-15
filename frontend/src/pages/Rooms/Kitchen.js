import KitchenContent from '../../components/KitchenContent';
import RoomsLayout from '../../components/RoomsLayout';

export default function LivingRoom() {
    return <RoomsLayout roomName="Kitchen" ContentComponent={KitchenContent} />;
};